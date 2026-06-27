package org.example.semantics;

import org.example.ast.Ast;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TypeChecker {

    private final SymbolTable symbolTable;
    private final SemanticErrorReporter errors;

    private final Deque<Map<String, MiniCType>> scopes =
            new ArrayDeque<>();

    private String currentFunctionName;
    private MiniCType currentReturnType;

    public TypeChecker(
            SymbolTable symbolTable,
            SemanticErrorReporter errors
    ) {
        this.symbolTable = symbolTable;
        this.errors = errors;
    }

    public void check(Ast.Node node) {
        if (node instanceof Ast.Program program) {
            checkProgram(program);
        } else {
            error("el TypeChecker espera un Ast.Program como raiz");
        }
    }

    private void checkProgram(Ast.Program program) {
        for (Ast.Node item : program.items) {
            if (item instanceof Ast.Function function) {
                checkFunction(function);
            } else if (item instanceof Ast.Declaration) {
                error("las variables globales aun no estan soportadas "
                        + "por el TypeChecker de Fase 2");
            }
        }
    }

    private void checkFunction(Ast.Function function) {
        currentFunctionName = function.name;
        currentReturnType = functionReturnType(function);

        pushScope();

        for (Ast.Param param : function.params) {
            MiniCType type = declaratorType(param.type, param.name);

            declare(baseName(param.name), type);
        }

        // El cuerpo principal comparte scope con parámetros.
        checkBlock(function.body, false);

        if (currentReturnType != null
                && !currentReturnType.getName().equals("void")
                && !containsReturn(function.body)) {

            error("la funcion '" + function.name
                    + "' debe retornar un valor de tipo '"
                    + currentReturnType + "'");
        }

        popScope();

        currentFunctionName = null;
        currentReturnType = null;
    }

    private void checkBlock(
            Ast.Block block,
            boolean createScope
    ) {
        if (createScope) {
            pushScope();
        }

        for (Ast.Node statement : block.items) {
            checkStatement(statement);
        }

        if (createScope) {
            popScope();
        }
    }

    private void checkStatement(Ast.Node statement) {
        if (statement == null) {
            return;
        }

        if (statement instanceof Ast.Declaration declaration) {
            checkDeclaration(declaration);

        } else if (statement instanceof Ast.AssignStmt assign) {
            checkAssign(assign);

        } else if (statement instanceof Ast.ReturnStmt returnStmt) {
            checkReturn(returnStmt);

        } else if (statement instanceof Ast.IfStmt ifStmt) {
            checkIf(ifStmt);

        } else if (statement instanceof Ast.WhileStmt whileStmt) {
            checkWhile(whileStmt);

        } else if (statement instanceof Ast.ExprStmt exprStmt) {
            typeOf(exprStmt.expr);

        } else if (statement instanceof Ast.Block block) {
            checkBlock(block, true);

        } else if (statement instanceof Ast.RawStmt) {
            error("for y do-while aun no tienen nodos AST propios "
                    + "para verificacion de tipos");
        }
    }

    private void checkDeclaration(Ast.Declaration declaration) {
        for (String rawName : declaration.names) {
            String name = baseName(rawName);

            MiniCType type = declaratorType(
                    declaration.type,
                    rawName
            );

            Map<String, MiniCType> currentScope = scopes.peek();

            if (currentScope.containsKey(name)) {
                // SymbolTableBuilder ya muestra el error con ubicación real.
                continue;
            }

            currentScope.put(name, type);
        }
    }

    private void checkAssign(Ast.AssignStmt assign) {
        MiniCType targetType = typeOfAssignable(assign.target);
        MiniCType valueType = typeOf(assign.value);

        if (targetType == null || valueType == null) {
            return;
        }

        if (!TypeRules.canAssign(targetType, valueType)) {
            error("no se puede asignar tipo '" + valueType
                    + "' a destino de tipo '" + targetType + "'");
        }
    }

    private void checkReturn(Ast.ReturnStmt returnStmt) {
        if (currentReturnType == null) {
            return;
        }

        if (currentReturnType.getName().equals("void")) {
            if (returnStmt.value != null) {
                error("la funcion void '" + currentFunctionName
                        + "' no debe retornar un valor");
            }

            return;
        }

        if (returnStmt.value == null) {
            error("la funcion '" + currentFunctionName
                    + "' debe retornar un valor de tipo '"
                    + currentReturnType + "'");
            return;
        }

        MiniCType valueType = typeOf(returnStmt.value);

        if (valueType != null &&
                !TypeRules.canAssign(currentReturnType, valueType)) {

            error("la funcion '" + currentFunctionName
                    + "' debe retornar '" + currentReturnType
                    + "' pero retorna '" + valueType + "'");
        }
    }

    private void checkIf(Ast.IfStmt ifStmt) {
        MiniCType conditionType = typeOf(ifStmt.condition);

        if (conditionType != null &&
                !TypeRules.isConditionType(conditionType)) {

            error("la condicion del if debe ser bool, int o char, no '"
                    + conditionType + "'");
        }

        checkStatement(ifStmt.thenBranch);

        if (ifStmt.elseBranch != null) {
            checkStatement(ifStmt.elseBranch);
        }
    }

    private void checkWhile(Ast.WhileStmt whileStmt) {
        MiniCType conditionType = typeOf(whileStmt.condition);

        if (conditionType != null &&
                !TypeRules.isConditionType(conditionType)) {

            error("la condicion del while debe ser bool, int o char, no '"
                    + conditionType + "'");
        }

        checkStatement(whileStmt.body);
    }

    private MiniCType typeOf(Ast.Expr expr) {
        if (expr == null) {
            return null;
        }

        if (expr instanceof Ast.Literal literal) {
            return literalType(literal);
        }

        if (expr instanceof Ast.Variable variable) {
            return lookup(variable.name);
        }

        if (expr instanceof Ast.BinaryExpr binary) {
            return typeOfBinary(binary);
        }

        if (expr instanceof Ast.UnaryExpr unary) {
            return typeOfUnary(unary);
        }

        if (expr instanceof Ast.Call call) {
            return typeOfCall(call);
        }

        if (expr instanceof Ast.RawExpr) {
            error("RawExpr sin tipo; ajusta MiniCASTBuilder");
            return null;
        }

        error("expresion no reconocida: "
                + expr.getClass().getSimpleName());

        return null;
    }

    private MiniCType typeOfBinary(Ast.BinaryExpr binary) {
        MiniCType left = typeOf(binary.left);
        MiniCType right = typeOf(binary.right);

        if (left == null || right == null) {
            return null;
        }

        return switch (binary.op) {
            case "+", "-", "*", "/", "%" -> {
                MiniCType result =
                        TypeRules.arithmeticResult(left, right);

                if (result == null) {
                    error("el operador '" + binary.op
                            + "' requiere operandos int o char, no '"
                            + left + "' y '" + right + "'");
                }

                yield result;
            }

            case "<", "<=", ">", ">=" -> {
                if (!TypeRules.isNumeric(left) ||
                        !TypeRules.isNumeric(right)) {

                    error("el operador relacional '"
                            + binary.op
                            + "' requiere operandos int o char");
                }

                yield new MiniCType("bool", false, 0);
            }

            case "==", "!=" -> {
                if (!TypeRules.canCompareEquality(left, right)) {
                    error("no se pueden comparar '"
                            + left + "' y '" + right + "'");
                }

                yield new MiniCType("bool", false, 0);
            }

            case "&&", "||" -> {
                if (!TypeRules.isConditionType(left) ||
                        !TypeRules.isConditionType(right)) {

                    error("el operador logico '"
                            + binary.op
                            + "' requiere bool, int o char");
                }

                yield new MiniCType("bool", false, 0);
            }

            default -> {
                error("operador binario no soportado: " + binary.op);
                yield null;
            }
        };
    }

    private MiniCType typeOfUnary(Ast.UnaryExpr unary) {
        MiniCType valueType = typeOf(unary.expr);

        if (valueType == null) {
            return null;
        }

        return switch (unary.op) {
            case "-" -> {
                if (!TypeRules.isNumeric(valueType)) {
                    error("el operador unario '-' requiere int o char");
                }

                yield new MiniCType("int", false, 0);
            }

            case "!" -> {
                if (!TypeRules.isConditionType(valueType)) {
                    error("el operador unario '!' requiere bool, int o char");
                }

                yield new MiniCType("bool", false, 0);
            }

            case "*", "&" -> {
                error("punteros aun no estan implementados "
                        + "en TypeChecker/IR/MIPS");
                yield null;
            }

            default -> {
                error("operador unario no soportado: " + unary.op);
                yield null;
            }
        };
    }

    private MiniCType typeOfCall(Ast.Call call) {
        Symbol symbol = symbolTable.resolve(call.name);

        if (!(symbol instanceof FunctionSymbol function)) {
            // SymbolTableBuilder ya marca funciones inexistentes.
            return null;
        }

        List<MiniCType> expected = function.getParamTypes();

        if (expected.size() != call.args.size()) {
            error("la funcion '" + call.name
                    + "' espera " + expected.size()
                    + " argumento(s) pero recibio "
                    + call.args.size());
        }

        int checkedArgs = Math.min(
                expected.size(),
                call.args.size()
        );

        for (int index = 0; index < checkedArgs; index++) {
            MiniCType actual = typeOf(call.args.get(index));
            MiniCType required = expected.get(index);

            if (actual != null &&
                    !TypeRules.canAssign(required, actual)) {

                error("el argumento " + (index + 1)
                        + " de '" + call.name
                        + "' debe ser '" + required
                        + "' pero recibio '" + actual + "'");
            }
        }

        return function.getType();
    }

    private MiniCType typeOfAssignable(String target) {
        if (target.startsWith("*")) {
            error("asignacion mediante punteros no implementada: "
                    + target);
            return null;
        }

        String base = baseName(target);

        MiniCType declared = lookup(base);

        if (declared == null) {
            return null;
        }

        int indexes = countIndexes(target);

        if (indexes == 0) {
            return declared;
        }

        if (declared.getDimensions() < indexes) {
            error("se usan demasiados indices para '" + base + "'");
            return null;
        }

        return new MiniCType(
                declared.getName(),
                declared.isPointer(),
                declared.getDimensions() - indexes
        );
    }

    private MiniCType literalType(Ast.Literal literal) {
        String value = literal.value;

        if ("true".equals(value) || "false".equals(value)) {
            return new MiniCType("bool", false, 0);
        }

        if (value.startsWith("'")) {
            return new MiniCType("char", false, 0);
        }

        if (value.startsWith("\"")) {
            return new MiniCType("string", false, 0);
        }

        return new MiniCType("int", false, 0);
    }

    private MiniCType functionReturnType(Ast.Function function) {
        Symbol symbol = symbolTable.resolve(function.name);

        if (symbol != null) {
            return symbol.getType();
        }

        return new MiniCType(function.returnType, false, 0);
    }

    private void pushScope() {
        scopes.push(new LinkedHashMap<>());
    }

    private void popScope() {
        scopes.pop();
    }

    private void declare(String name, MiniCType type) {
        scopes.peek().put(name, type);
    }

    private MiniCType lookup(String name) {
        for (Map<String, MiniCType> scope : scopes) {
            MiniCType type = scope.get(name);

            if (type != null) {
                return type;
            }
        }

        Symbol globalSymbol = symbolTable.resolve(name);

        return globalSymbol == null
                ? null
                : globalSymbol.getType();
    }

    private boolean containsReturn(Ast.Node node) {
        if (node instanceof Ast.ReturnStmt) {
            return true;
        }

        if (node instanceof Ast.Block block) {
            for (Ast.Node item : block.items) {
                if (containsReturn(item)) {
                    return true;
                }
            }
        }

        if (node instanceof Ast.IfStmt ifStmt) {
            return containsReturn(ifStmt.thenBranch)
                    || (ifStmt.elseBranch != null
                    && containsReturn(ifStmt.elseBranch));
        }

        if (node instanceof Ast.WhileStmt whileStmt) {
            return containsReturn(whileStmt.body);
        }

        return false;
    }

    private MiniCType declaratorType(
            String baseType,
            String declarator
    ) {
        boolean pointer = declarator.startsWith("*");
        int dimensions = countIndexes(declarator);

        return new MiniCType(baseType, pointer, dimensions);
    }

    private String baseName(String declarator) {
        String name = declarator;

        while (name.startsWith("*")) {
            name = name.substring(1);
        }

        int bracket = name.indexOf('[');

        return bracket >= 0
                ? name.substring(0, bracket)
                : name;
    }

    private int countIndexes(String declarator) {
        int count = 0;

        for (int index = 0; index < declarator.length(); index++) {
            if (declarator.charAt(index) == '[') {
                count++;
            }
        }

        return count;
    }

    private void error(String message) {
        // El AST actual aún no conserva línea/columna.
        errors.addError(0, 0, message);
    }
}