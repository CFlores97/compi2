package org.example.semantics;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.example.antlr.MiniCBaseListener;
import org.example.antlr.MiniCParser;

import java.util.ArrayList;
import java.util.List;

/** Builds lexical scopes and symbol entries from the Parse Tree. */
public final class SymbolTableBuilder extends MiniCBaseListener {
    private final SymbolTable symbolTable;
    private final SemanticErrorReporter errors;
    private int blockCounter;
    private boolean enteringFunctionBody;

    public SymbolTableBuilder(SymbolTable symbolTable, SemanticErrorReporter errors) {
        this.symbolTable = symbolTable;
        this.errors = errors;
    }

    /**
     * First pass: makes all user function signatures visible. This allows calls
     * to functions declared later in the source file.
     */
    public void collectFunctionSignatures(MiniCParser.ProgramContext program) {
        for (MiniCParser.FuncDefContext function : program.funcDef()) {
            String name = function.Identifier().getText();
            Token token = function.Identifier().getSymbol();
            List<MiniCType> params = new ArrayList<>();
            if (function.params() != null) {
                for (MiniCParser.ParamContext param : function.params().param()) {
                    params.add(typeOf(param.typeSpecifier().getText(), param.declarator()));
                }
            }
            FunctionSymbol symbol = new FunctionSymbol(name,
                    MiniCType.scalar(function.typeSpecifier().getText()), params,
                    token.getLine(), token.getCharPositionInLine() + 1);
            Symbol existing = symbolTable.resolveGlobal(name);
            // Runtime identifiers cannot be redefined either.
            if (existing != null || !symbolTable.defineGlobal(symbol)) {
                errors.addError(token.getLine(), token.getCharPositionInLine() + 1,
                        "la funcion '" + name + "' ya fue declarada");
            }
        }
    }

    @Override
    public void enterFuncDef(MiniCParser.FuncDefContext ctx) {
        symbolTable.enterScope("funcion " + ctx.Identifier().getText());
        enteringFunctionBody = true;
    }

    @Override
    public void exitFuncDef(MiniCParser.FuncDefContext ctx) {
        symbolTable.exitScope();
    }

    @Override
    public void enterParam(MiniCParser.ParamContext ctx) {
        define(ctx.declarator(), ctx.typeSpecifier().getText(), SymbolKind.PARAMETER);
    }

    @Override
    public void enterDeclaration(MiniCParser.DeclarationContext ctx) {
        for (MiniCParser.InitDeclaratorContext init : ctx.initDeclaratorList().initDeclarator()) {
            SymbolKind kind = init.declarator().IntegerConst().isEmpty() ? SymbolKind.VARIABLE : SymbolKind.ARRAY;
            define(init.declarator(), ctx.typeSpecifier().getText(), kind);
        }
    }

    @Override
    public void enterCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        if (enteringFunctionBody) {
            enteringFunctionBody = false;
            return;
        }
        symbolTable.enterScope("bloque " + (++blockCounter));
    }

    @Override
    public void exitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        if (!(ctx.getParent() instanceof MiniCParser.FuncDefContext)) symbolTable.exitScope();
    }

    @Override
    public void enterLvalue(MiniCParser.LvalueContext ctx) {
        if (ctx.Identifier() == null) return; // outer *lvalue; nested lvalue validates its identifier
        String name = ctx.Identifier().getText();
        if (symbolTable.resolve(name) == null) {
            Token token = ctx.Identifier().getSymbol();
            errors.addError(token.getLine(), token.getCharPositionInLine() + 1,
                    "la variable '" + name + "' no ha sido declarada");
        }
    }

    @Override
    public void enterCallExpr(MiniCParser.CallExprContext ctx) {
        String name = ctx.Identifier().getText();
        Symbol symbol = symbolTable.resolveGlobal(name);
        if (symbol == null) {
            Token token = ctx.Identifier().getSymbol();
            errors.addError(token.getLine(), token.getCharPositionInLine() + 1,
                    "la funcion '" + name + "' no ha sido declarada");
        } else if (!(symbol instanceof FunctionSymbol)) {
            Token token = ctx.Identifier().getSymbol();
            errors.addError(token.getLine(), token.getCharPositionInLine() + 1,
                    "'" + name + "' existe pero no es una funcion");
        }
    }

    private void define(MiniCParser.DeclaratorContext declarator, String baseType, SymbolKind kind) {
        Token token = declarator.Identifier().getSymbol();
        String name = declarator.Identifier().getText();
        Symbol symbol = new Symbol(name, kind, typeOf(baseType, declarator), token.getLine(), token.getCharPositionInLine() + 1);
        if (!symbolTable.define(symbol)) {
            errors.addError(token.getLine(), token.getCharPositionInLine() + 1,
                    "'" + name + "' ya fue declarado en este ambito");
        }
    }

    private MiniCType typeOf(String baseType, MiniCParser.DeclaratorContext declarator) {
        String text = declarator.getText();
        int pointers = 0;
        while (pointers < text.length() && text.charAt(pointers) == '*') pointers++;
        List<Integer> dimensions = new ArrayList<>();
        declarator.IntegerConst().forEach(node -> dimensions.add(Integer.parseInt(node.getText())));
        return new MiniCType(baseType, pointers, dimensions);
    }

    // Registra las funciones del runtime en el ámbito global
// para que el TypeChecker no las marque como no declaradas
    public void registerBuiltins() {
        // Funciones void del runtime
        String[] voidFuncs = { "println", "print_int", "print_char", "print_bool", "print_str", "read_str" };
        for (String name : voidFuncs) {
            symbolTable.defineGlobal(new FunctionSymbol(name,
                    MiniCType.scalar("void"),
                    new ArrayList<>(), 0, 0));
        }

        // Funciones que retornan int
        symbolTable.defineGlobal(new FunctionSymbol("read_int",
                MiniCType.scalar("int"),
                new ArrayList<>(), 0, 0));

        // Funciones que retornan char
        symbolTable.defineGlobal(new FunctionSymbol("read_char",
                MiniCType.scalar("char"),
                new ArrayList<>(), 0, 0));
    }
}
