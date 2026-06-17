package org.example.semantics;

import org.antlr.v4.runtime.Token;
import org.example.antlr.MiniCBaseListener;
import org.example.antlr.MiniCParser;
import java.util.ArrayList;
import java.util.List;

// Recorre el árbol de parseo generado por ANTLR4
// y construye la tabla de símbolos con sus ámbitos
public class SymbolTableBuilder extends MiniCBaseListener {

    private final SymbolTable symbolTable;
    private final SemanticErrorReporter errors;

    // Contador para nombrar bloques internos: bloque 1, bloque 2, etc.
    private int blockCounter = 0;

    // Bandera para no abrir un ámbito doble en el cuerpo de una función
    // (ya se abrió en enterFuncDef, no hay que abrirlo de nuevo en enterCompoundStmt)
    private boolean enteringFunctionBody = false;

    public SymbolTableBuilder(SymbolTable symbolTable, SemanticErrorReporter errors) {
        this.symbolTable = symbolTable;
        this.errors      = errors;
    }

    //  FUNCIONES

    // Al entrar a una función: registrar su nombre en el ámbito global
    // y abrir un nuevo ámbito para sus parámetros y variables locales
    @Override
    public void enterFuncDef(MiniCParser.FuncDefContext ctx) {
        String name     = ctx.Identifier().getText();
        String typeName = ctx.typeSpecifier().getText();
        Token  token    = ctx.Identifier().getSymbol();

        // Construir la lista de tipos de parámetros ANTES de definir la función
        List<MiniCType> paramTypes = new ArrayList<>();
        if (ctx.params() != null) {
            for (MiniCParser.ParamContext p : ctx.params().param()) {
                String pType = p.typeSpecifier().getText();
                int    dims  = countDimensions(p.declarator());
                boolean ptr  = isPointer(p.declarator());
                paramTypes.add(new MiniCType(pType, ptr, dims));
            }
        }

        FunctionSymbol sym = new FunctionSymbol(name,
                new MiniCType(typeName, false, 0),
                paramTypes,
                token.getLine(), token.getCharPositionInLine());

        if (!symbolTable.define(sym))
            errors.addError(token.getLine(), token.getCharPositionInLine(),
                    "la funcion '" + name + "' ya fue declarada");

        symbolTable.enterScope("funcion " + name);
        enteringFunctionBody = true;
    }


    // Al salir de una función: cerrar su ámbito
    @Override
    public void exitFuncDef(MiniCParser.FuncDefContext ctx) {
        symbolTable.exitScope();
    }

    //  PARÁMETROS

    // Registra cada parámetro dentro del ámbito de la función
    @Override
    public void enterParam(MiniCParser.ParamContext ctx) {
        String typeName = ctx.typeSpecifier().getText();
        String name     = getDeclaratorName(ctx.declarator());
        Token  token    = getDeclaratorToken(ctx.declarator());
        int    dims     = countDimensions(ctx.declarator());
        boolean ptr     = isPointer(ctx.declarator());

        Symbol sym = new Symbol(name, SymbolKind.PARAMETER,
                new MiniCType(typeName, ptr, dims),
                token.getLine(), token.getCharPositionInLine());

        if (!symbolTable.define(sym))
            errors.addError(token.getLine(), token.getCharPositionInLine(),
                    "el parametro '" + name + "' ya fue declarado");
    }

    // DECLARACIONES

    // Registra cada variable o arreglo declarado
    // Si tiene corchetes se guarda como ARRAY, si no como VARIABLE
    @Override
    public void enterDeclaration(MiniCParser.DeclarationContext ctx) {
        String typeName = ctx.typeSpecifier().getText();

        for (MiniCParser.DeclaratorContext decl : ctx.declaratorList().declarator()) {
            String name  = getDeclaratorName(decl);
            Token  token = getDeclaratorToken(decl);
            int    dims  = countDimensions(decl);
            boolean ptr  = isPointer(decl);

            SymbolKind kind = dims > 0 ? SymbolKind.ARRAY : SymbolKind.VARIABLE;

            Symbol sym = new Symbol(name, kind,
                    new MiniCType(typeName, ptr, dims),
                    token.getLine(), token.getCharPositionInLine());

            // Si ya existe en el mismo ámbito, es redeclaración
            if (!symbolTable.define(sym))
                errors.addError(token.getLine(), token.getCharPositionInLine(),
                        "'" + name + "' ya fue declarado en este ambito");
        }
    }

    // BLOQUES

    // Al entrar a un bloque {}: abrir nuevo ámbito
    // Excepción: el cuerpo de una función ya tiene su ámbito abierto
    @Override
    public void enterCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        if (enteringFunctionBody) {
            enteringFunctionBody = false;
            return;
        }
        blockCounter++;
        symbolTable.enterScope("bloque " + blockCounter);
    }

    // Al salir de un bloque {}: cerrar el ámbito
    // Excepción: el cuerpo principal de función se cierra en exitFuncDef
    @Override
    public void exitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        if (ctx.getParent() instanceof MiniCParser.FuncDefContext) return;
        symbolTable.exitScope();
    }

    //  VALIDAR USO DE VARIABLES

    // Cada vez que aparece un lvalue, verifica que la variable fue declarada
    @Override
    public void enterLvalue(MiniCParser.LvalueContext ctx) {
        String name  = ctx.Identifier().getText();
        Token  token = ctx.Identifier().getSymbol();

        if (symbolTable.resolve(name) == null)
            errors.addError(token.getLine(), token.getCharPositionInLine(),
                    "la variable '" + name + "' no ha sido declarada");
    }

    //  VALIDAR LLAMADAS A FUNCIONES

    // Cada vez que aparece una llamada, verifica que la función fue declarada
    @Override
    public void enterCallExpr(MiniCParser.CallExprContext ctx) {
        String name  = ctx.Identifier().getText();
        Token  token = ctx.Identifier().getSymbol();

        Symbol found = symbolTable.resolve(name);

        if (found == null)
            errors.addError(token.getLine(), token.getCharPositionInLine(),
                    "la funcion '" + name + "' no ha sido declarada");
        else if (found.getKind() != SymbolKind.FUNCTION)
            errors.addError(token.getLine(), token.getCharPositionInLine(),
                    "'" + name + "' existe pero no es una funcion");
    }

    // HELPERS para declarator recursivo
    // La gramática permite punteros encadenados (* declarator),
    // así que hay que bajar recursivamente hasta encontrar el Identifier

    // Obtiene el nombre del identificador en un declarator
    private String getDeclaratorName(MiniCParser.DeclaratorContext ctx) {
        if (ctx.Identifier() != null) return ctx.Identifier().getText();
        return getDeclaratorName(ctx.declarator());
    }

    // Obtiene el token del identificador para reportar línea y columna
    private Token getDeclaratorToken(MiniCParser.DeclaratorContext ctx) {
        if (ctx.Identifier() != null) return ctx.Identifier().getSymbol();
        return getDeclaratorToken(ctx.declarator());
    }

    // Cuenta cuántas dimensiones de arreglo tiene el declarator
    private int countDimensions(MiniCParser.DeclaratorContext ctx) {
        if (ctx.Identifier() != null) return ctx.IntegerConst().size();
        return countDimensions(ctx.declarator());
    }

    // Detecta si el declarator es un puntero (* declarator)
    private boolean isPointer(MiniCParser.DeclaratorContext ctx) {
        if (ctx.Identifier() != null) return false;
        return true;
    }
}