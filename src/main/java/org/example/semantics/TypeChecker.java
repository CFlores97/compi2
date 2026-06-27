package org.example.semantics;

import org.example.ast.Ast;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Semantic type checker over the custom AST. */
public final class TypeChecker {
    private final SymbolTable symbols;
    private final SemanticErrorReporter errors;
    private final Deque<Map<String, MiniCType>> scopes = new ArrayDeque<>();
    private String currentFunction;
    private MiniCType currentReturnType;

    public TypeChecker(SymbolTable symbols, SemanticErrorReporter errors) {
        this.symbols = symbols;
        this.errors = errors;
    }

    public void check(Ast.Node node) {
        if (!(node instanceof Ast.Program program)) {
            error(node, "el TypeChecker espera Ast.Program como raiz");
            return;
        }
        checkProgram(program);
    }

    private void checkProgram(Ast.Program program) {
        pushScope(); // Global declarations from AST.
        for (Ast.Node item : program.items) {
            if (item instanceof Ast.Declaration declaration) checkDeclaration(declaration);
        }
        for (Ast.Node item : program.items) {
            if (item instanceof Ast.Function function) checkFunction(function);
        }
        popScope();
    }

    private void checkFunction(Ast.Function function) {
        currentFunction = function.name;
        Symbol symbol = symbols.resolveGlobal(function.name);
        currentReturnType = symbol == null ? MiniCType.scalar(function.returnType) : symbol.getType();

        pushScope();
        for (Ast.Param parameter : function.params) {
            MiniCType type = typeFor(parameter.type, parameter.declarator);
            declare(parameter.declarator.name, type, parameter);
        }

        // Function body shares the parameter scope.
        checkBlock(function.body, false);

        if (!"void".equals(currentReturnType.getName()) && !definitelyReturns(function.body)) {
            error(function, "la funcion '" + function.name + "' puede terminar sin return de tipo '" + currentReturnType + "'");
        }

        popScope();
        currentFunction = null;
        currentReturnType = null;
    }

    private void checkBlock(Ast.Block block, boolean newScope) {
        if (newScope) pushScope();
        for (Ast.Node item : block.items) {
            if (item instanceof Ast.Stmt stmt) checkStmt(stmt);
        }
        if (newScope) popScope();
    }

    private void checkStmt(Ast.Stmt statement) {
        if (statement instanceof Ast.Declaration declaration) {
            checkDeclaration(declaration);
        } else if (statement instanceof Ast.AssignStmt assign) {
            checkAssign(assign);
        } else if (statement instanceof Ast.ReturnStmt returnStmt) {
            checkReturn(returnStmt);
        } else if (statement instanceof Ast.ExprStmt exprStmt) {
            if (exprStmt.expr != null) typeOf(exprStmt.expr);
        } else if (statement instanceof Ast.Block block) {
            checkBlock(block, true);
        } else if (statement instanceof Ast.IfStmt ifStmt) {
            checkCondition(ifStmt.condition, "if");
            checkStmt(ifStmt.thenBranch);
            if (ifStmt.elseBranch != null) checkStmt(ifStmt.elseBranch);
        } else if (statement instanceof Ast.WhileStmt whileStmt) {
            checkCondition(whileStmt.condition, "while");
            checkStmt(whileStmt.body);
        } else if (statement instanceof Ast.ForStmt forStmt) {
            pushScope();
            if (forStmt.init != null) checkStmt(forStmt.init);
            if (forStmt.condition != null) checkCondition(forStmt.condition, "for");
            if (forStmt.step != null) checkStmt(forStmt.step);
            checkStmt(forStmt.body);
            popScope();
        } else if (statement instanceof Ast.DoWhileStmt doWhileStmt) {
            checkStmt(doWhileStmt.body);
            checkCondition(doWhileStmt.condition, "do-while");
        }
    }

    private void checkDeclaration(Ast.Declaration declaration) {
        if ("void".equals(declaration.type)) {
            error(declaration, "no se pueden declarar variables de tipo void");
            return;
        }
        for (Ast.Declarator declarator : declaration.declarators) {
            MiniCType type = typeFor(declaration.type, declarator);
            declare(declarator.name, type, declarator);
            if (declarator.initializer != null) {
                if (type.isArray()) {
                    error(declarator, "la inicializacion de arreglos aun debe hacerse elemento por elemento");
                } else {
                    MiniCType value = typeOf(declarator.initializer);
                    if (value != null && !TypeRules.canAssign(type, value)) {
                        error(declarator, "no se puede inicializar '" + declarator.name + "' de tipo '" + type + "' con '" + value + "'");
                    }
                }
            }
        }
    }

    private void declare(String name, MiniCType type, Ast.Node node) {
        Map<String, MiniCType> current = scopes.peek();
        if (current.containsKey(name)) {
            // The symbol builder has already reported a precise redeclaration error.
            return;
        }
        current.put(name, type);
    }

    private void checkAssign(Ast.AssignStmt assign) {
        MiniCType target = typeOfLValue(assign.target, true);
        MiniCType value = typeOf(assign.value);
        if (target != null && value != null && !TypeRules.canAssign(target, value)) {
            error(assign, "no se puede asignar tipo '" + value + "' a destino de tipo '" + target + "'");
        }
    }

    private void checkReturn(Ast.ReturnStmt returnStmt) {
        if (currentReturnType == null) return;
        if ("void".equals(currentReturnType.getName())) {
            if (returnStmt.value != null) error(returnStmt, "la funcion void '" + currentFunction + "' no debe retornar un valor");
            return;
        }
        if (returnStmt.value == null) {
            error(returnStmt, "la funcion '" + currentFunction + "' debe retornar un valor de tipo '" + currentReturnType + "'");
            return;
        }
        MiniCType value = typeOf(returnStmt.value);
        if (value != null && !TypeRules.canAssign(currentReturnType, value)) {
            error(returnStmt, "la funcion '" + currentFunction + "' debe retornar '" + currentReturnType + "' pero retorna '" + value + "'");
        }
    }

    private void checkCondition(Ast.Expr condition, String owner) {
        MiniCType type = typeOf(condition);
        if (type != null && !TypeRules.isConditionType(type)) {
            error(condition, "la condicion de " + owner + " debe ser bool, int o char, no '" + type + "'");
        }
    }

    private MiniCType typeOf(Ast.Expr expr) {
        if (expr == null) return null;
        if (expr instanceof Ast.Literal literal) return literalType(literal);
        if (expr instanceof Ast.LValue lValue) return typeOfLValue(lValue, false);
        if (expr instanceof Ast.BinaryExpr binary) return typeOfBinary(binary);
        if (expr instanceof Ast.UnaryExpr unary) return typeOfUnary(unary);
        if (expr instanceof Ast.Call call) return typeOfCall(call);
        error(expr, "expresion no reconocida: " + expr.getClass().getSimpleName());
        return null;
    }

    private MiniCType typeOfLValue(Ast.LValue lValue, boolean assignmentTarget) {
        if (lValue instanceof Ast.VariableLValue variable) {
            MiniCType type = lookup(variable.name);
            if (type == null) {
                error(variable, "la variable '" + variable.name + "' no ha sido declarada");
                return null;
            }
            if (!assignmentTarget && type.isArray()) {
                // Array-to-pointer decay in expressions and function arguments.
                return new MiniCType(type.getName(), type.getPointerDepth() + 1,
                        type.getDimensionSizes().subList(1, type.getDimensionSizes().size()));
            }
            if (assignmentTarget && type.isArray()) {
                error(variable, "no se puede asignar un arreglo completo: '" + variable.name + "'");
                return null;
            }
            return type;
        }
        if (lValue instanceof Ast.ArrayLValue array) {
            MiniCType base = lookup(array.name);
            if (base == null) {
                error(array, "el arreglo '" + array.name + "' no ha sido declarado");
                return null;
            }
            if (!base.isArray()) {
                error(array, "'" + array.name + "' no es un arreglo");
                return null;
            }
            if (array.indices.size() > base.getDimensions()) {
                error(array, "se usan demasiados indices para '" + array.name + "'");
                return null;
            }
            for (Ast.Expr index : array.indices) {
                MiniCType indexType = typeOf(index);
                if (indexType != null && !TypeRules.isIndexType(indexType)) {
                    error(index, "el indice de un arreglo debe ser int o char, no '" + indexType + "'");
                }
            }
            MiniCType result = base.elementTypeAfter(array.indices.size());
            if (assignmentTarget && result.isArray()) {
                error(array, "falta indice para seleccionar un elemento de '" + array.name + "'");
                return null;
            }
            if (!assignmentTarget && result.isArray()) {
                return new MiniCType(result.getName(), result.getPointerDepth() + 1,
                        result.getDimensionSizes().subList(1, result.getDimensionSizes().size()));
            }
            return result;
        }
        if (lValue instanceof Ast.DerefLValue deref) {
            MiniCType pointer = typeOf(deref.pointer);
            if (pointer == null) return null;
            MiniCType result = pointer.dereferenceType();
            if (result == null) {
                error(deref, "no se puede desreferenciar un valor que no es puntero: '" + pointer + "'");
            }
            return result;
        }
        error(lValue, "lvalue no reconocido");
        return null;
    }

    private MiniCType typeOfBinary(Ast.BinaryExpr binary) {
        MiniCType left = typeOf(binary.left);
        MiniCType right = typeOf(binary.right);
        if (left == null || right == null) return null;
        return switch (binary.op) {
            case "+", "-", "*", "/", "%" -> {
                MiniCType result = TypeRules.arithmeticResult(left, right);
                if (result == null) error(binary, "el operador '" + binary.op + "' requiere operandos int o char");
                yield result;
            }
            case "<", "<=", ">", ">=" -> {
                if (!TypeRules.isNumeric(left) || !TypeRules.isNumeric(right)) error(binary, "el operador relacional '" + binary.op + "' requiere int o char");
                yield MiniCType.scalar("bool");
            }
            case "==", "!=" -> {
                if (!TypeRules.canCompareEquality(left, right)) error(binary, "no se pueden comparar '" + left + "' y '" + right + "'");
                yield MiniCType.scalar("bool");
            }
            case "&&", "||" -> {
                if (!TypeRules.isConditionType(left) || !TypeRules.isConditionType(right)) error(binary, "el operador logico '" + binary.op + "' requiere bool, int o char");
                yield MiniCType.scalar("bool");
            }
            default -> {
                error(binary, "operador no soportado: " + binary.op);
                yield null;
            }
        };
    }

    private MiniCType typeOfUnary(Ast.UnaryExpr unary) {
        MiniCType type = typeOf(unary.expr);
        if (type == null) return null;
        return switch (unary.op) {
            case "-" -> {
                if (!TypeRules.isNumeric(type)) error(unary, "el operador '-' requiere int o char");
                yield MiniCType.scalar("int");
            }
            case "!" -> {
                if (!TypeRules.isConditionType(type)) error(unary, "el operador '!' requiere bool, int o char");
                yield MiniCType.scalar("bool");
            }
            case "&" -> {
                if (!(unary.expr instanceof Ast.LValue lValue)) {
                    error(unary, "el operador '&' requiere una variable, arreglo o lvalue");
                    yield null;
                }
                MiniCType lType = typeOfLValue(lValue, false);
                yield lType == null ? null : lType.addressType();
            }
            case "*" -> {
                MiniCType deref = type.dereferenceType();
                if (deref == null) error(unary, "el operador '*' requiere un puntero");
                yield deref;
            }
            default -> {
                error(unary, "operador unario no soportado: " + unary.op);
                yield null;
            }
        };
    }

    private MiniCType typeOfCall(Ast.Call call) {
        Symbol symbol = symbols.resolveGlobal(call.name);
        if (!(symbol instanceof FunctionSymbol function)) {
            error(call, "la funcion '" + call.name + "' no ha sido declarada");
            return null;
        }
        List<MiniCType> expected = function.getParamTypes();
        if (expected.size() != call.args.size()) {
            error(call, "la funcion '" + call.name + "' espera " + expected.size() + " argumento(s), pero recibio " + call.args.size());
        }
        for (int i = 0; i < Math.min(expected.size(), call.args.size()); i++) {
            MiniCType actual = typeOf(call.args.get(i));
            MiniCType required = expected.get(i);
            if (actual != null && !argumentCompatible(required, actual)) {
                error(call.args.get(i), "el argumento " + (i + 1) + " de '" + call.name + "' debe ser '" + required + "', pero recibio '" + actual + "'");
            }
        }
        return function.getType();
    }

    private boolean argumentCompatible(MiniCType required, MiniCType actual) {
        if (TypeRules.canAssign(required, actual)) return true;
        // A declared array decays to pointer when passed to a pointer parameter.
        return required != null && actual != null
                && required.getPointerDepth() > 0
                && required.getName().equals(actual.getName())
                && actual.getPointerDepth() > 0;
    }

    private MiniCType literalType(Ast.Literal literal) {
        return switch (literal.kind) {
            case INT -> MiniCType.scalar("int");
            case CHAR -> MiniCType.scalar("char");
            case BOOL -> MiniCType.scalar("bool");
            case STRING -> MiniCType.scalar("string");
        };
    }

    private MiniCType typeFor(String base, Ast.Declarator declarator) {
        return new MiniCType(base, declarator.pointerDepth, declarator.dimensions);
    }

    private MiniCType lookup(String name) {
        for (Map<String, MiniCType> scope : scopes) {
            MiniCType type = scope.get(name);
            if (type != null) return type;
        }
        Symbol global = symbols.resolveGlobal(name);
        return global == null ? null : global.getType();
    }

    private boolean definitelyReturns(Ast.Stmt statement) {
        if (statement instanceof Ast.ReturnStmt) return true;
        if (statement instanceof Ast.Block block) {
            for (Ast.Node item : block.items) if (item instanceof Ast.Stmt stmt && definitelyReturns(stmt)) return true;
            return false;
        }
        if (statement instanceof Ast.IfStmt ifStmt) {
            return ifStmt.elseBranch != null && definitelyReturns(ifStmt.thenBranch) && definitelyReturns(ifStmt.elseBranch);
        }
        return false;
    }

    private void pushScope() { scopes.push(new LinkedHashMap<>()); }
    private void popScope() { scopes.pop(); }

    private void error(Ast.Node node, String message) {
        int line = node == null ? 0 : node.line();
        int column = node == null ? 0 : node.column();
        errors.addError(line, column, message);
    }
}
