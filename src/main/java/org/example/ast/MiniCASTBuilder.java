package org.example.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.example.antlr.MiniCBaseVisitor;
import org.example.antlr.MiniCParser;

import java.util.ArrayList;
import java.util.List;

public class MiniCASTBuilder extends MiniCBaseVisitor<Ast.Node> {
    @Override
    public Ast.Node visitProgram(MiniCParser.ProgramContext ctx) {
        List<Ast.Node> items = new ArrayList<>();
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) continue;
            Ast.Node node = visit(child);
            if (node != null) {
                items.add(node);
            }
        }
        return new Ast.Program(items);
    }

    @Override
    public Ast.Node visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String returnType = ctx.typeSpecifier().getText();
        String name = ctx.Identifier().getText();
        List<Ast.Param> params = new ArrayList<>();
        if (ctx.params() != null) {
            for (MiniCParser.ParamContext paramCtx : ctx.params().param()) {
                Ast.Node node = visit(paramCtx);
                if (node instanceof Ast.Param) {
                    params.add((Ast.Param) node);
                }
            }
        }
        Ast.Block body = (Ast.Block) visit(ctx.compoundStmt());
        return new Ast.Function(returnType, name, params, body);
    }

    @Override
    public Ast.Node visitParam(MiniCParser.ParamContext ctx) {
        String type = ctx.typeSpecifier().getText();
        String name = declaratorToString(ctx.declarator());
        return new Ast.Param(type, name);
    }

    @Override
    public Ast.Node visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String type = ctx.typeSpecifier().getText();
        List<String> names = new ArrayList<>();
        for (MiniCParser.DeclaratorContext declarator :
                ctx.declaratorList().declarator()) {
            names.add(declaratorToString(declarator));
        }
        return new Ast.Declaration(type, names);
    }

    @Override
    public Ast.Node visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        List<Ast.Node> items = new ArrayList<>();
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) continue;
            Ast.Node node = visit(child);
            if (node != null) {
                items.add(node);
            }
        }
        return new Ast.Block(items);
    }

    @Override
    public Ast.Node visitStatement(MiniCParser.StatementContext ctx) {
        if (ctx.getChildCount() == 0) return null;
        return visit(ctx.getChild(0));
    }

    @Override
    public Ast.Node visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        Ast.Expr value = null;
        if (ctx.expr() != null) {
            Ast.Node exprNode = visit(ctx.expr());
            value = (exprNode instanceof Ast.Expr) ? (Ast.Expr) exprNode : new Ast.RawExpr(ctx.expr().getText());
        }
        return new Ast.ReturnStmt(value);
    }

    @Override
    public Ast.Node visitAssignStmt(MiniCParser.AssignStmtContext ctx) {
        String target = ctx.lvalue().getText();
        Ast.Node exprNode = visit(ctx.expr());
        Ast.Expr value = (exprNode instanceof Ast.Expr) ? (Ast.Expr) exprNode : new Ast.RawExpr(ctx.expr().getText());
        return new Ast.AssignStmt(target, value);
    }

    @Override
    public Ast.Node visitExprStmt(MiniCParser.ExprStmtContext ctx) {
        if (ctx.expr() == null) return new Ast.ExprStmt(null);
        Ast.Node exprNode = visit(ctx.expr());
        Ast.Expr expr = (exprNode instanceof Ast.Expr) ? (Ast.Expr) exprNode : new Ast.RawExpr(ctx.expr().getText());
        return new Ast.ExprStmt(expr);
    }

    @Override
    public Ast.Node visitIfStmt(MiniCParser.IfStmtContext ctx) {
        Ast.Node condNode = visit(ctx.expr());
        Ast.Expr condition = (condNode instanceof Ast.Expr) ? (Ast.Expr) condNode : new Ast.RawExpr(ctx.expr().getText());
        Ast.Node thenBranch = visit(ctx.statement(0));
        Ast.Node elseBranch = null;
        if (ctx.statement().size() > 1) {
            elseBranch = visit(ctx.statement(1));
        }
        return new Ast.IfStmt(condition, thenBranch, elseBranch);
    }

    @Override
    public Ast.Node visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        Ast.Node condNode = visit(ctx.expr());
        Ast.Expr condition = (condNode instanceof Ast.Expr) ? (Ast.Expr) condNode : new Ast.RawExpr(ctx.expr().getText());
        Ast.Node body = visit(ctx.statement());
        return new Ast.WhileStmt(condition, body);
    }

    @Override
    public Ast.Node visitForStmt(MiniCParser.ForStmtContext ctx) {
        return new Ast.RawStmt("for " + ctx.getText());
    }

    @Override
    public Ast.Node visitDoWhileStmt(MiniCParser.DoWhileStmtContext ctx) {
        return new Ast.RawStmt("do-while " + ctx.getText());
    }

    @Override
    public Ast.Node visitLvalue(MiniCParser.LvalueContext ctx) {
        return new Ast.Variable(ctx.getText());
    }

    @Override
    public Ast.Node visitCallExpr(MiniCParser.CallExprContext ctx) {

        String name = ctx.Identifier().getText();
        List<Ast.Expr> args = new ArrayList<>();
        for (MiniCParser.ExprContext exprCtx : ctx.expr()) {
            args.add(new Ast.RawExpr(exprCtx.getText()));
        }
        return new Ast.Call(name, args);
    }

    // Literales
    @Override
    public Ast.Node visitIntLiteral(MiniCParser.IntLiteralContext ctx) {
        return new Ast.Literal(ctx.getText());
    }

    @Override
    public Ast.Node visitTrueLiteral(MiniCParser.TrueLiteralContext ctx) {
        return new Ast.Literal("true");
    }

    @Override
    public Ast.Node visitFalseLiteral(MiniCParser.FalseLiteralContext ctx) {
        return new Ast.Literal("false");
    }

    @Override
    public Ast.Node visitCharLiteral(MiniCParser.CharLiteralContext ctx) {
        return new Ast.Literal(ctx.getText());
    }

    @Override
    public Ast.Node visitStringLiteralExpr(MiniCParser.StringLiteralExprContext ctx) {
        return new Ast.Literal(ctx.getText());
    }

    // Variable usada en expresión
    @Override
    public Ast.Node visitLvalueExpr(MiniCParser.LvalueExprContext ctx) {
        return new Ast.Variable(ctx.lvalue().Identifier().getText());
    }

    // Expresiones binarias
    @Override
    public Ast.Node visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {
        Ast.Node left  = visit(ctx.expr(0));
        Ast.Node right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        return new Ast.BinaryExpr(toExpr(left, ctx.expr(0)), op, toExpr(right, ctx.expr(1)));
    }

    @Override
    public Ast.Node visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {
        Ast.Node left  = visit(ctx.expr(0));
        Ast.Node right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        return new Ast.BinaryExpr(toExpr(left, ctx.expr(0)), op, toExpr(right, ctx.expr(1)));
    }

    @Override
    public Ast.Node visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {
        Ast.Node left  = visit(ctx.expr(0));
        Ast.Node right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        return new Ast.BinaryExpr(toExpr(left, ctx.expr(0)), op, toExpr(right, ctx.expr(1)));
    }

    @Override
    public Ast.Node visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        Ast.Node left  = visit(ctx.expr(0));
        Ast.Node right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        return new Ast.BinaryExpr(toExpr(left, ctx.expr(0)), op, toExpr(right, ctx.expr(1)));
    }

    @Override
    public Ast.Node visitAndExpr(MiniCParser.AndExprContext ctx) {
        Ast.Node left  = visit(ctx.expr(0));
        Ast.Node right = visit(ctx.expr(1));
        return new Ast.BinaryExpr(toExpr(left, ctx.expr(0)), "&&", toExpr(right, ctx.expr(1)));
    }

    @Override
    public Ast.Node visitOrExpr(MiniCParser.OrExprContext ctx) {
        Ast.Node left  = visit(ctx.expr(0));
        Ast.Node right = visit(ctx.expr(1));
        return new Ast.BinaryExpr(toExpr(left, ctx.expr(0)), "||", toExpr(right, ctx.expr(1)));
    }

    @Override
    public Ast.Node visitParenExpr(MiniCParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Ast.Node visitUnaryExpr(MiniCParser.UnaryExprContext ctx) {
        String op = ctx.getChild(0).getText();
        Ast.Node expr = visit(ctx.expr());
        return new Ast.UnaryExpr(op, toExpr(expr, ctx.expr()));
    }

    private String declaratorToString(MiniCParser.DeclaratorContext ctx) {
        if (ctx.Identifier() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(ctx.Identifier().getText());
            for (TerminalNode size : ctx.IntegerConst()) {
                sb.append("[").append(size.getText()).append("]");
            }
            return sb.toString();
        }
        return "*" + declaratorToString(ctx.declarator());
    }

    // Convierte un Node a Expr, usando RawExpr como fallback
    private Ast.Expr toExpr(Ast.Node node, MiniCParser.ExprContext ctx) {
        if (node instanceof Ast.Expr) return (Ast.Expr) node;
        return new Ast.RawExpr(ctx.getText());
    }
}