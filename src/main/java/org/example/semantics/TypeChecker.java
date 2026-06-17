package org.example.semantics;

import org.example.ast.Ast;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Recorre el AST y verifica reglas de tipos:
// asignaciones compatibles, llamadas correctas y returns válidos
public class TypeChecker {

    private final SymbolTable symbolTable;
    private final SemanticErrorReporter errors;

    // Nombre y tipo de retorno de la función que se está revisando
    private String currentFunctionName = null;
    private MiniCType currentReturnType = null;

    // Mapa de variables del ámbito actual: nombre -> tipo
    // Se llena al entrar a cada función para poder verificar asignaciones
    private Map<String, MiniCType> localVarTypes = new HashMap<>();

    public TypeChecker(SymbolTable symbolTable, SemanticErrorReporter errors) {
        this.symbolTable = symbolTable;
        this.errors      = errors;
    }

    // Punto de entrada: recibe el nodo raíz del AST
    public void check(Ast.Node node) {
        if (node instanceof Ast.Program) {
            checkProgram((Ast.Program) node);
        }
    }

    // Recorre todas las funciones del programa
    private void checkProgram(Ast.Program program) {
        for (Ast.Node child : program.items) {
            if (child instanceof Ast.Function) {
                checkFunction((Ast.Function) child);
            }
        }
    }

    // Verifica una función: llena el mapa local, guarda el tipo de retorno
    // y revisa el cuerpo
    private void checkFunction(Ast.Function func) {
        currentFunctionName = func.name;
        localVarTypes.clear();

        // Obtiene el tipo de retorno desde la tabla de símbolos
        Symbol sym = symbolTable.resolve(func.name);
        if (sym != null) {
            currentReturnType = sym.getType();
        }

        // Agrega los parámetros al mapa local
        for (Ast.Param param : func.params) {
            localVarTypes.put(param.name, new MiniCType(param.type, false, 0));
        }

        // Recorre las declaraciones del bloque y las agrega al mapa local
        collectDeclarations(func.body);

        checkBlock(func.body);

        currentFunctionName = null;
        currentReturnType   = null;
        localVarTypes.clear();
    }

    // Recorre el bloque buscando declaraciones para llenar localVarTypes
    private void collectDeclarations(Ast.Block block) {
        for (Ast.Node stmt : block.items) {
            if (stmt instanceof Ast.Declaration) {
                Ast.Declaration decl = (Ast.Declaration) stmt;
                for (String name : decl.names) {
                    // Quitar dimensiones del nombre si tiene arreglo: "a[10]" -> "a"
                    String cleanName = name.contains("[")
                            ? name.substring(0, name.indexOf("["))
                            : name;
                    localVarTypes.put(cleanName, new MiniCType(decl.type, false, 0));
                }
            } else if (stmt instanceof Ast.Block) {
                collectDeclarations((Ast.Block) stmt);
            }
        }
    }

    // Revisa cada sentencia dentro de un bloque
    private void checkBlock(Ast.Block block) {
        for (Ast.Node stmt : block.items) {
            checkStatement(stmt);
        }
    }

    // Despacha según el tipo de sentencia
    private void checkStatement(Ast.Node stmt) {
        if (stmt instanceof Ast.AssignStmt) {
            checkAssign((Ast.AssignStmt) stmt);
        } else if (stmt instanceof Ast.ReturnStmt) {
            checkReturn((Ast.ReturnStmt) stmt);
        } else if (stmt instanceof Ast.IfStmt) {
            checkIf((Ast.IfStmt) stmt);
        } else if (stmt instanceof Ast.WhileStmt) {
            checkWhile((Ast.WhileStmt) stmt);
        } else if (stmt instanceof Ast.Block) {
            checkBlock((Ast.Block) stmt);
        } else if (stmt instanceof Ast.ExprStmt) {
            checkExpr(((Ast.ExprStmt) stmt).expr);
        }
    }

    // Verifica que la asignación sea entre tipos compatibles
    private void checkAssign(Ast.AssignStmt assign) {
        MiniCType rightType = checkExpr(assign.value);

        // Quitar índices si es arreglo: "a[0]" -> "a"
        String targetName = assign.target.contains("[")
                ? assign.target.substring(0, assign.target.indexOf("["))
                : assign.target;

        // Busca el tipo en el mapa local primero
        MiniCType leftType = localVarTypes.get(targetName);

        if (leftType == null) return; // Ya reportado por SymbolTableBuilder

        if (rightType != null && !TypeRules.canAssign(leftType, rightType)) {
            errors.addError(0, 0,
                    "no se puede asignar tipo '" + rightType
                            + "' a variable de tipo '" + leftType + "'");
        }
    }

    // Verifica que el return sea compatible con el tipo de la función
    private void checkReturn(Ast.ReturnStmt ret) {
        if (currentReturnType == null) return;

        // Función void con return de valor
        if (currentReturnType.getName().equals("void") && ret.value != null) {
            errors.addError(0, 0,
                    "la funcion '" + currentFunctionName
                            + "' es void y no debe retornar un valor");
            return;
        }

        // Función no void sin return de valor
        if (!currentReturnType.getName().equals("void") && ret.value == null) {
            errors.addError(0, 0,
                    "la funcion '" + currentFunctionName
                            + "' debe retornar un valor de tipo '" + currentReturnType + "'");
            return;
        }

        // Verifica compatibilidad del tipo retornado
        if (ret.value != null) {
            MiniCType retType = checkExpr(ret.value);
            if (retType != null && !TypeRules.canAssign(currentReturnType, retType)) {
                errors.addError(0, 0,
                        "la funcion '" + currentFunctionName
                                + "' debe retornar '" + currentReturnType
                                + "' pero se retorna '" + retType + "'");
            }
        }
    }

    // Verifica que la condición del if sea un tipo válido
    private void checkIf(Ast.IfStmt ifStmt) {
        MiniCType condType = checkExpr(ifStmt.condition);
        if (condType != null && !TypeRules.isConditionType(condType)) {
            errors.addError(0, 0,
                    "la condicion del if debe ser bool, int o char, no '"
                            + condType + "'");
        }
        checkStatement(ifStmt.thenBranch);
        if (ifStmt.elseBranch != null) checkStatement(ifStmt.elseBranch);
    }

    // Verifica que la condición del while sea un tipo válido
    private void checkWhile(Ast.WhileStmt whileStmt) {
        MiniCType condType = checkExpr(whileStmt.condition);
        if (condType != null && !TypeRules.isConditionType(condType)) {
            errors.addError(0, 0,
                    "la condicion del while debe ser bool, int o char, no '"
                            + condType + "'");
        }
        checkStatement(whileStmt.body);
    }

    // Infiere el tipo de una expresión
    private MiniCType checkExpr(Ast.Node expr) {
        if (expr == null) return null;

        if (expr instanceof Ast.Literal) {
            return inferLiteralType((Ast.Literal) expr);
        }

        if (expr instanceof Ast.Variable) {
            String varName = ((Ast.Variable) expr).name;
            // Busca primero en el mapa local, luego en la tabla global
            MiniCType localType = localVarTypes.get(varName);
            if (localType != null) return localType;
            Symbol sym = symbolTable.resolve(varName);
            if (sym == null) return null;
            return sym.getType();
        }

        if (expr instanceof Ast.BinaryExpr) {
            return checkBinaryExpr((Ast.BinaryExpr) expr);
        }

        if (expr instanceof Ast.UnaryExpr) {
            return checkExpr(((Ast.UnaryExpr) expr).expr);
        }

        if (expr instanceof Ast.Call) {
            return checkCallExpr((Ast.Call) expr);
        }

        // RawExpr no tiene tipo estático claro todavía
        return null;
    }

    // Infiere el tipo de un literal por su contenido
    private MiniCType inferLiteralType(Ast.Literal lit) {
        String val = lit.value;
        if (val.equals("true") || val.equals("false"))
            return new MiniCType("bool", false, 0);
        if (val.startsWith("'"))
            return new MiniCType("char", false, 0);
        if (val.startsWith("\""))
            return new MiniCType("string", false, 0);
        return new MiniCType("int", false, 0);
    }

    // Verifica una expresión binaria y retorna su tipo resultante
    private MiniCType checkBinaryExpr(Ast.BinaryExpr bin) {
        MiniCType left  = checkExpr(bin.left);
        MiniCType right = checkExpr(bin.right);

        if (left == null || right == null) return null;

        // Operadores relacionales, igualdad y lógicos retornan bool
        if (bin.op.equals("<") || bin.op.equals(">")
                || bin.op.equals("<=") || bin.op.equals(">=")
                || bin.op.equals("==") || bin.op.equals("!=")
                || bin.op.equals("&&") || bin.op.equals("||")) {
            return new MiniCType("bool", false, 0);
        }

        // Operadores aritméticos retornan el tipo del lado izquierdo
        return left;
    }

    // Verifica una llamada a función: existencia y número de argumentos
    private MiniCType checkCallExpr(Ast.Call call) {
        Symbol sym = symbolTable.resolve(call.name);
        if (sym == null) return null; // Ya reportado por SymbolTableBuilder

        if (!(sym instanceof FunctionSymbol)) {
            errors.addError(0, 0, "'" + call.name + "' no es una funcion");
            return null;
        }

        FunctionSymbol func = (FunctionSymbol) sym;
        List<MiniCType> expected = func.getParamTypes();
        List<Ast.Expr>  actual   = call.args;

        // Verificar número de argumentos
        if (actual.size() != expected.size()) {
            errors.addError(0, 0,
                    "la funcion '" + call.name + "' espera "
                            + expected.size() + " argumento(s) pero recibio "
                            + actual.size());
        }

        return func.getType();
    }
}