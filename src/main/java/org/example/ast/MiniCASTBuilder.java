package org.example.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.example.antlr.MiniCBaseVisitor;
import org.example.antlr.MiniCParser;

import java.util.ArrayList;
import java.util.List;

/** Builds the custom AST from ANTLR's Parse Tree. */
public class MiniCASTBuilder extends MiniCBaseVisitor<Ast.Node> {

    @Override
    public Ast.Node visitProgram(MiniCParser.ProgramContext ctx) {
        List<Ast.Node> items = new ArrayList<>();
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) continue;
            Ast.Node node = visit(child);
            if (node != null) items.add(node);
        }
        return new Ast.Program(line(ctx), column(ctx), items);
    }

    @Override
    public Ast.Node visitFuncDef(MiniCParser.FuncDefContext ctx) {
        List<Ast.Param> params = new ArrayList<>();
        if (ctx.params() != null) {
            for (MiniCParser.ParamContext param : ctx.params().param()) {
                params.add((Ast.Param) visit(param));
            }
        }
        return new Ast.Function(line(ctx), column(ctx), ctx.typeSpecifier().getText(),
                ctx.Identifier().getText(), params, (Ast.Block) visit(ctx.compoundStmt()));
    }

    @Override
    public Ast.Node visitParam(MiniCParser.ParamContext ctx) {
        return new Ast.Param(line(ctx), column(ctx), ctx.typeSpecifier().getText(),
                buildDeclarator(ctx.declarator(), null));
    }

    @Override
    public Ast.Node visitDeclaration(MiniCParser.DeclarationContext ctx) {
        List<Ast.Declarator> declarators = new ArrayList<>();
        for (MiniCParser.InitDeclaratorContext init : ctx.initDeclaratorList().initDeclarator()) {
            Ast.Expr initializer = init.expr() == null ? null : asExpr(visit(init.expr()), init);
            declarators.add(buildDeclarator(init.declarator(), initializer));
        }
        return new Ast.Declaration(line(ctx), column(ctx), ctx.typeSpecifier().getText(), declarators);
    }

    @Override
    public Ast.Node visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        List<Ast.Node> items = new ArrayList<>();
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) continue;
            Ast.Node node = visit(child);
            if (node != null) items.add(node);
        }
        return new Ast.Block(line(ctx), column(ctx), items);
    }

    @Override
    public Ast.Node visitStatement(MiniCParser.StatementContext ctx) {
        return ctx.getChildCount() == 0 ? null : visit(ctx.getChild(0));
    }

    @Override
    public Ast.Node visitAssignStmt(MiniCParser.AssignStmtContext ctx) {
        return new Ast.AssignStmt(line(ctx), column(ctx), asLValue(visit(ctx.lvalue()), ctx),
                asExpr(visit(ctx.expr()), ctx));
    }

    @Override
    public Ast.Node visitAssignNoSemi(MiniCParser.AssignNoSemiContext ctx) {
        return new Ast.AssignStmt(line(ctx), column(ctx), asLValue(visit(ctx.lvalue()), ctx),
                asExpr(visit(ctx.expr()), ctx));
    }

    @Override
    public Ast.Node visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        Ast.Expr value = ctx.expr() == null ? null : asExpr(visit(ctx.expr()), ctx);
        return new Ast.ReturnStmt(line(ctx), column(ctx), value);
    }

    @Override
    public Ast.Node visitExprStmt(MiniCParser.ExprStmtContext ctx) {
        Ast.Expr expr = ctx.expr() == null ? null : asExpr(visit(ctx.expr()), ctx);
        return new Ast.ExprStmt(line(ctx), column(ctx), expr);
    }

    @Override
    public Ast.Node visitIfStmt(MiniCParser.IfStmtContext ctx) {
        Ast.Expr condition = asExpr(visit(ctx.expr()), ctx);
        Ast.Stmt thenBranch = asStmt(visit(ctx.statement(0)), ctx);
        Ast.Stmt elseBranch = ctx.statement().size() > 1 ? asStmt(visit(ctx.statement(1)), ctx) : null;
        return new Ast.IfStmt(line(ctx), column(ctx), condition, thenBranch, elseBranch);
    }

    @Override
    public Ast.Node visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        return new Ast.WhileStmt(line(ctx), column(ctx), asExpr(visit(ctx.expr()), ctx),
                asStmt(visit(ctx.statement()), ctx));
    }

    @Override
    public Ast.Node visitForStmt(MiniCParser.ForStmtContext ctx) {
        Ast.Stmt init = buildForPart(ctx.forInit());
        Ast.Expr condition = ctx.expr() == null ? null : asExpr(visit(ctx.expr()), ctx);
        Ast.Stmt step = buildForPart(ctx.forStep());
        Ast.Stmt body = asStmt(visit(ctx.statement()), ctx);
        return new Ast.ForStmt(line(ctx), column(ctx), init, condition, step, body);
    }

    @Override
    public Ast.Node visitDoWhileStmt(MiniCParser.DoWhileStmtContext ctx) {
        return new Ast.DoWhileStmt(line(ctx), column(ctx), asStmt(visit(ctx.statement()), ctx),
                asExpr(visit(ctx.expr()), ctx));
    }

    @Override
    public Ast.Node visitLvalue(MiniCParser.LvalueContext ctx) {
        if (ctx.Identifier() != null) {
            String name = ctx.Identifier().getText();
            if (ctx.expr().isEmpty()) return new Ast.VariableLValue(line(ctx), column(ctx), name);
            List<Ast.Expr> indexes = new ArrayList<>();
            for (MiniCParser.ExprContext index : ctx.expr()) indexes.add(asExpr(visit(index), index));
            return new Ast.ArrayLValue(line(ctx), column(ctx), name, indexes);
        }
        return new Ast.DerefLValue(line(ctx), column(ctx), asExpr(visit(ctx.lvalue()), ctx));
    }

    @Override
    public Ast.Node visitLvalueExpr(MiniCParser.LvalueExprContext ctx) {
        return visit(ctx.lvalue());
    }

    @Override
    public Ast.Node visitCallExpr(MiniCParser.CallExprContext ctx) {
        List<Ast.Expr> args = new ArrayList<>();
        for (MiniCParser.ExprContext arg : ctx.expr()) args.add(asExpr(visit(arg), arg));
        return new Ast.Call(line(ctx), column(ctx), ctx.Identifier().getText(), args);
    }

    @Override public Ast.Node visitIntLiteral(MiniCParser.IntLiteralContext ctx) {
        return new Ast.Literal(line(ctx), column(ctx), Ast.LiteralKind.INT, ctx.getText());
    }
    @Override public Ast.Node visitCharLiteral(MiniCParser.CharLiteralContext ctx) {
        return new Ast.Literal(line(ctx), column(ctx), Ast.LiteralKind.CHAR, ctx.getText());
    }
    @Override public Ast.Node visitStringLiteralExpr(MiniCParser.StringLiteralExprContext ctx) {
        return new Ast.Literal(line(ctx), column(ctx), Ast.LiteralKind.STRING, ctx.getText());
    }
    @Override public Ast.Node visitTrueLiteral(MiniCParser.TrueLiteralContext ctx) {
        return new Ast.Literal(line(ctx), column(ctx), Ast.LiteralKind.BOOL, "true");
    }
    @Override public Ast.Node visitFalseLiteral(MiniCParser.FalseLiteralContext ctx) {
        return new Ast.Literal(line(ctx), column(ctx), Ast.LiteralKind.BOOL, "false");
    }

    @Override public Ast.Node visitParenExpr(MiniCParser.ParenExprContext ctx) { return visit(ctx.expr()); }

    @Override
    public Ast.Node visitUnaryExpr(MiniCParser.UnaryExprContext ctx) {
        String op = ctx.getChild(0).getText();
        return new Ast.UnaryExpr(line(ctx), column(ctx), op, asExpr(visit(ctx.expr()), ctx));
    }

    @Override public Ast.Node visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) { return binary(ctx, ctx.getChild(1).getText()); }
    @Override public Ast.Node visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) { return binary(ctx, ctx.getChild(1).getText()); }
    @Override public Ast.Node visitRelationalExpr(MiniCParser.RelationalExprContext ctx) { return binary(ctx, ctx.getChild(1).getText()); }
    @Override public Ast.Node visitEqualityExpr(MiniCParser.EqualityExprContext ctx) { return binary(ctx, ctx.getChild(1).getText()); }
    @Override public Ast.Node visitAndExpr(MiniCParser.AndExprContext ctx) { return binary(ctx, "&&"); }
    @Override public Ast.Node visitOrExpr(MiniCParser.OrExprContext ctx) { return binary(ctx, "||"); }

    private Ast.Node binary(ParserRuleContext ctx, String op) {
        List<MiniCParser.ExprContext> expressions =
                ctx.getRuleContexts(MiniCParser.ExprContext.class);

        if (expressions.size() != 2) {
            throw new IllegalStateException(
                    "Se esperaban dos expresiones para el operador '" + op
                            + "' en la linea " + line(ctx)
            );
        }

        Ast.Expr left = asExpr(
                visit(expressions.get(0)),
                expressions.get(0)
        );

        Ast.Expr right = asExpr(
                visit(expressions.get(1)),
                expressions.get(1)
        );

        return new Ast.BinaryExpr(
                line(ctx),
                column(ctx),
                left,
                op,
                right
        );
    }

    private Ast.Declarator buildDeclarator(MiniCParser.DeclaratorContext ctx, Ast.Expr initializer) {
        String text = ctx.getText();
        int pointers = 0;
        while (pointers < text.length() && text.charAt(pointers) == '*') pointers++;
        List<Integer> dimensions = new ArrayList<>();
        for (TerminalNode dimension : ctx.IntegerConst()) dimensions.add(Integer.parseInt(dimension.getText()));
        return new Ast.Declarator(line(ctx), column(ctx), ctx.Identifier().getText(), pointers, dimensions, initializer);
    }

    private Ast.Stmt buildForPart(ParserRuleContext ctx) {
        if (ctx instanceof MiniCParser.ForInitContext init) {
            if (init.assignNoSemi() != null) return asStmt(visit(init.assignNoSemi()), init);
            if (init.expr() != null) return new Ast.ExprStmt(line(init), column(init), asExpr(visit(init.expr()), init));
            return null;
        }
        if (ctx instanceof MiniCParser.ForStepContext step) {
            if (step.assignNoSemi() != null) return asStmt(visit(step.assignNoSemi()), step);
            if (step.expr() != null) return new Ast.ExprStmt(line(step), column(step), asExpr(visit(step.expr()), step));
            return null;
        }
        return null;
    }

    private Ast.Expr asExpr(Ast.Node node, ParserRuleContext ctx) {
        if (node instanceof Ast.Expr expr) return expr;
        throw new IllegalStateException("Se esperaba expresion cerca de linea " + line(ctx));
    }
    private Ast.LValue asLValue(Ast.Node node, ParserRuleContext ctx) {
        if (node instanceof Ast.LValue value) return value;
        throw new IllegalStateException("Se esperaba lvalue cerca de linea " + line(ctx));
    }
    private Ast.Stmt asStmt(Ast.Node node, ParserRuleContext ctx) {
        if (node instanceof Ast.Stmt stmt) return stmt;
        throw new IllegalStateException("Se esperaba sentencia cerca de linea " + line(ctx));
    }
    private int line(ParserRuleContext ctx) { return ctx.getStart() == null ? 0 : ctx.getStart().getLine(); }
    private int column(ParserRuleContext ctx) { return ctx.getStart() == null ? 0 : ctx.getStart().getCharPositionInLine() + 1; }
}
