package org.example.IR;

import org.example.ast.Ast;
import org.example.semantics.MiniCType;
import org.example.semantics.SymbolTable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converts a semantically valid AST into three-address code. */
public final class IRGenerator {
    private enum Storage { LOCAL, PARAM, GLOBAL }
    private record Binding(String sourceName, String irName, MiniCType type, Storage storage) { }

    private final TempFactory temps = new TempFactory();
    private final LabelFactory labels = new LabelFactory();
    @SuppressWarnings("unused")
    private final SymbolTable symbols;

    private final Map<String, Binding> globals = new LinkedHashMap<>();
    private final Deque<Map<String, Binding>> scopes = new ArrayDeque<>();
    private int uniqueCounter;
    private IRProgram program;
    private IRFunction current;

    public IRGenerator(SymbolTable symbols) { this.symbols = symbols; }

    public IRProgram generate(Ast.Node root) {
        if (!(root instanceof Ast.Program programNode)) {
            throw new IllegalArgumentException("IRGenerator espera Ast.Program como raiz");
        }
        temps.reset(); labels.reset(); uniqueCounter = 0; globals.clear(); scopes.clear();
        program = new IRProgram();

        // First pass: allocate global storage before generating any function.
        for (Ast.Node item : programNode.items) {
            if (item instanceof Ast.Declaration declaration) genGlobalDeclaration(declaration);
        }
        // Second pass: generate function bodies.
        for (Ast.Node item : programNode.items) {
            if (item instanceof Ast.Function function) genFunction(function);
        }
        return program;
    }

    private void genGlobalDeclaration(Ast.Declaration declaration) {
        for (Ast.Declarator declarator : declaration.declarators) {
            MiniCType type = typeFor(declaration.type, declarator);
            String label = "g_" + declarator.name;
            if (globals.containsKey(declarator.name)) throw new IllegalStateException("Global duplicada: " + declarator.name);
            Operand initializer = null;
            if (declarator.initializer != null) initializer = globalInitializer(declarator.initializer);
            if (type.isArray() && initializer != null) throw unsupported("inicializacion global de arreglos");
            globals.put(declarator.name, new Binding(declarator.name, label, type, Storage.GLOBAL));
            program.addGlobal(new IRGlobal(label, type, initializer));
        }
    }

    private Operand globalInitializer(Ast.Expr expr) {
        if (expr instanceof Ast.Literal literal) return literalOperand(literal);
        if (expr instanceof Ast.UnaryExpr unary && "-".equals(unary.op) && unary.expr instanceof Ast.Literal literal && literal.kind == Ast.LiteralKind.INT) {
            return Operand.constant("-" + literal.value);
        }
        throw unsupported("inicializador global no constante");
    }

    private void genFunction(Ast.Function function) {
        current = new IRFunction(function.name);
        scopes.clear();
        pushScope();
        for (Ast.Param param : function.params) {
            MiniCType type = typeFor(param.type, param.declarator);
            String irName = unique(param.declarator.name);
            Binding binding = new Binding(param.declarator.name, irName, type, Storage.PARAM);
            declare(binding, param);
            current.addParam(irName, type);
        }
        // Function body uses the same scope as parameters.
        genBlock(function.body, false);
        popScope();
        program.addFunction(current);
        current = null;
    }

    private void genBlock(Ast.Block block, boolean createScope) {
        if (createScope) pushScope();
        for (Ast.Node item : block.items) {
            if (item instanceof Ast.Stmt statement) genStmt(statement);
        }
        if (createScope) popScope();
    }

    private void genStmt(Ast.Stmt statement) {
        if (statement == null) return;
        if (statement instanceof Ast.Declaration declaration) genLocalDeclaration(declaration);
        else if (statement instanceof Ast.Block block) genBlock(block, true);
        else if (statement instanceof Ast.AssignStmt assign) genAssign(assign);
        else if (statement instanceof Ast.ReturnStmt ret) current.emit(IRInstruction.ret(ret.value == null ? null : genExpr(ret.value)));
        else if (statement instanceof Ast.ExprStmt exprStmt) { if (exprStmt.expr != null) genExprStatement(exprStmt.expr); }
        else if (statement instanceof Ast.IfStmt ifStmt) genIf(ifStmt);
        else if (statement instanceof Ast.WhileStmt whileStmt) genWhile(whileStmt);
        else if (statement instanceof Ast.ForStmt forStmt) genFor(forStmt);
        else if (statement instanceof Ast.DoWhileStmt doWhileStmt) genDoWhile(doWhileStmt);
        else throw new UnsupportedOperationException("Sentencia AST desconocida: " + statement.getClass().getSimpleName());
    }

    private void genLocalDeclaration(Ast.Declaration declaration) {
        for (Ast.Declarator declarator : declaration.declarators) {
            MiniCType type = typeFor(declaration.type, declarator);
            String irName = unique(declarator.name);
            Binding binding = new Binding(declarator.name, irName, type, Storage.LOCAL);
            declare(binding, declarator);
            current.addLocal(irName, type);
            if (declarator.initializer != null) {
                if (type.isArray()) throw unsupported("inicializacion de arreglos locales");
                current.emit(IRInstruction.assign(storageOperand(binding), genExpr(declarator.initializer)));
            }
        }
    }

    private void genAssign(Ast.AssignStmt assign) {
        Operand value = genExpr(assign.value);
        if (assign.target instanceof Ast.VariableLValue variable) {
            Binding binding = resolve(variable.name, variable);
            if (binding.type.isArray()) throw unsupported("asignacion de arreglo completo");
            current.emit(IRInstruction.assign(storageOperand(binding), value));
            return;
        }
        Operand address = genAddressOf(assign.target);
        current.emit(IRInstruction.storeIndirect(address, value));
    }

    private void genExprStatement(Ast.Expr expr) {
        if (expr instanceof Ast.Call call) {
            genCall(call, false);
        } else {
            genExpr(expr); // preserve possible side effects in future expressions
        }
    }

    private Operand genExpr(Ast.Expr expr) {
        if (expr instanceof Ast.Literal literal) return literalOperand(literal);
        if (expr instanceof Ast.VariableLValue variable) {
            Binding binding = resolve(variable.name, variable);
            if (binding.type.isArray()) return arrayBaseAddress(binding);
            return storageOperand(binding);
        }
        if (expr instanceof Ast.ArrayLValue array) {
            Operand address = genAddressOf(array);
            Operand result = temps.newTemp();
            current.emit(IRInstruction.loadIndirect(result, address));
            return result;
        }
        if (expr instanceof Ast.DerefLValue deref) {
            Operand address = genExpr(deref.pointer);
            Operand result = temps.newTemp();
            current.emit(IRInstruction.loadIndirect(result, address));
            return result;
        }
        if (expr instanceof Ast.BinaryExpr binary) {
            Operand result = temps.newTemp();
            current.emit(IRInstruction.bin(op(binary.op), result, genExpr(binary.left), genExpr(binary.right)));
            return result;
        }
        if (expr instanceof Ast.UnaryExpr unary) return genUnary(unary);
        if (expr instanceof Ast.Call call) return genCall(call, true);
        throw new UnsupportedOperationException("Expresion AST desconocida: " + expr.getClass().getSimpleName());
    }

    private Operand genUnary(Ast.UnaryExpr unary) {
        return switch (unary.op) {
            case "-" -> { Operand result = temps.newTemp(); current.emit(IRInstruction.unary(IROp.NEG, result, genExpr(unary.expr))); yield result; }
            case "!" -> { Operand result = temps.newTemp(); current.emit(IRInstruction.unary(IROp.NOT, result, genExpr(unary.expr))); yield result; }
            case "&" -> {
                if (!(unary.expr instanceof Ast.LValue lValue)) throw unsupported("operador & sobre una expresion que no es lvalue");
                yield genAddressOf(lValue);
            }
            case "*" -> { Operand result = temps.newTemp(); current.emit(IRInstruction.loadIndirect(result, genExpr(unary.expr))); yield result; }
            default -> throw new IllegalArgumentException("Operador unario no soportado: " + unary.op);
        };
    }

    private Operand genCall(Ast.Call call, boolean resultNeeded) {
        for (Ast.Expr arg : call.args) current.emit(IRInstruction.param(genExpr(arg)));
        Operand result = resultNeeded ? temps.newTemp() : null;
        current.emit(IRInstruction.call(result, Operand.function(call.name), call.args.size()));
        return result;
    }

    private void genIf(Ast.IfStmt statement) {
        Operand condition = genExpr(statement.condition);
        if (statement.elseBranch == null) {
            Operand end = labels.newLabel("if_end_");
            current.emit(IRInstruction.ifz(condition, end));
            genStmt(statement.thenBranch);
            current.emit(IRInstruction.label(end));
            return;
        }
        Operand elseLabel = labels.newLabel("if_else_");
        Operand endLabel = labels.newLabel("if_end_");
        current.emit(IRInstruction.ifz(condition, elseLabel));
        genStmt(statement.thenBranch);
        current.emit(IRInstruction.goTo(endLabel));
        current.emit(IRInstruction.label(elseLabel));
        genStmt(statement.elseBranch);
        current.emit(IRInstruction.label(endLabel));
    }

    private void genWhile(Ast.WhileStmt statement) {
        Operand start = labels.newLabel("while_start_");
        Operand end = labels.newLabel("while_end_");
        current.emit(IRInstruction.label(start));
        current.emit(IRInstruction.ifz(genExpr(statement.condition), end));
        genStmt(statement.body);
        current.emit(IRInstruction.goTo(start));
        current.emit(IRInstruction.label(end));
    }

    private void genFor(Ast.ForStmt statement) {
        pushScope();
        if (statement.init != null) genStmt(statement.init);
        Operand start = labels.newLabel("for_start_");
        Operand end = labels.newLabel("for_end_");
        current.emit(IRInstruction.label(start));
        if (statement.condition != null) current.emit(IRInstruction.ifz(genExpr(statement.condition), end));
        genStmt(statement.body);
        if (statement.step != null) genStmt(statement.step);
        current.emit(IRInstruction.goTo(start));
        current.emit(IRInstruction.label(end));
        popScope();
    }

    private void genDoWhile(Ast.DoWhileStmt statement) {
        Operand start = labels.newLabel("do_start_");
        Operand end = labels.newLabel("do_end_");
        current.emit(IRInstruction.label(start));
        genStmt(statement.body);
        current.emit(IRInstruction.ifz(genExpr(statement.condition), end));
        current.emit(IRInstruction.goTo(start));
        current.emit(IRInstruction.label(end));
    }

    /** Returns an address for x, a[i], m[i][j], or *p. */
    private Operand genAddressOf(Ast.LValue lValue) {
        if (lValue instanceof Ast.VariableLValue variable) {
            Binding binding = resolve(variable.name, variable);
            return addressOfBinding(binding);
        }
        if (lValue instanceof Ast.ArrayLValue array) {
            Binding binding = resolve(array.name, array);
            if (!binding.type.isArray()) throw unsupported("indexacion de una variable que no es arreglo: " + array.name);
            Operand base = arrayBaseAddress(binding);
            Operand linear = genLinearIndex(binding.type, array.indices, array);
            Operand byteOffset = temps.newTemp();
            current.emit(IRInstruction.bin(IROp.MUL, byteOffset, linear, Operand.constant("4")));
            Operand address = temps.newTemp();
            current.emit(IRInstruction.bin(IROp.ADD, address, base, byteOffset));
            return address;
        }
        if (lValue instanceof Ast.DerefLValue deref) return genExpr(deref.pointer);
        throw new UnsupportedOperationException("Lvalue desconocido");
    }

    private Operand arrayBaseAddress(Binding binding) {
        // An array parameter already contains its base address. A local/global array needs &storage.
        if (binding.storage == Storage.PARAM) return storageOperand(binding);
        return addressOfBinding(binding);
    }

    private Operand addressOfBinding(Binding binding) {
        Operand result = temps.newTemp();
        current.emit(IRInstruction.addressOf(result, storageOperand(binding)));
        return result;
    }

    private Operand genLinearIndex(MiniCType type, List<Ast.Expr> indices, Ast.Node where) {
        if (indices.isEmpty() || indices.size() > type.getDimensions()) throw unsupported("numero de indices invalido para arreglo");
        Operand linear = genExpr(indices.get(0));
        List<Integer> dimensions = type.getDimensionSizes();
        for (int i = 1; i < indices.size(); i++) {
            int columns = dimensions.get(i);
            Operand multiplied = temps.newTemp();
            current.emit(IRInstruction.bin(IROp.MUL, multiplied, linear, Operand.constant(String.valueOf(columns))));
            Operand next = genExpr(indices.get(i));
            Operand sum = temps.newTemp();
            current.emit(IRInstruction.bin(IROp.ADD, sum, multiplied, next));
            linear = sum;
        }
        return linear;
    }

    private Operand literalOperand(Ast.Literal literal) {
        return switch (literal.kind) {
            case INT -> Operand.constant(literal.value);
            case BOOL -> Operand.constant("true".equals(literal.value) ? "1" : "0");
            case CHAR -> Operand.constant(String.valueOf(parseChar(literal.value)));
            case STRING -> program.internString(literal.value);
        };
    }

    private int parseChar(String literal) {
        String content = literal.substring(1, literal.length() - 1);
        if (content.length() == 1) return content.charAt(0);
        return switch (content) {
            case "\\n" -> '\n'; case "\\t" -> '\t'; case "\\r" -> '\r'; case "\\0" -> 0;
            case "\\\\" -> '\\'; case "\\'" -> '\'';
            default -> throw new IllegalArgumentException("Char no soportado: " + literal);
        };
    }

    private IROp op(String operator) {
        return switch (operator) {
            case "+" -> IROp.ADD; case "-" -> IROp.SUB; case "*" -> IROp.MUL; case "/" -> IROp.DIV; case "%" -> IROp.MOD;
            case "<" -> IROp.LT; case "<=" -> IROp.LE; case ">" -> IROp.GT; case ">=" -> IROp.GE;
            case "==" -> IROp.EQ; case "!=" -> IROp.NE; case "&&" -> IROp.AND; case "||" -> IROp.OR;
            default -> throw new IllegalArgumentException("Operador no soportado: " + operator);
        };
    }

    private MiniCType typeFor(String base, Ast.Declarator declarator) {
        return new MiniCType(base, declarator.pointerDepth, declarator.dimensions);
    }

    private void pushScope() { scopes.push(new LinkedHashMap<>()); }
    private void popScope() { scopes.pop(); }
    private void declare(Binding binding, Ast.Node where) {
        Map<String, Binding> currentScope = scopes.peek();
        if (currentScope.containsKey(binding.sourceName)) throw new IllegalStateException("Redeclaracion durante IR: " + binding.sourceName);
        currentScope.put(binding.sourceName, binding);
    }

    private Binding resolve(String sourceName, Ast.Node where) {
        for (Map<String, Binding> scope : scopes) {
            Binding binding = scope.get(sourceName);
            if (binding != null) return binding;
        }
        Binding global = globals.get(sourceName);
        if (global != null) return global;
        throw new IllegalStateException("Identificador sin binding IR: " + sourceName + " en linea " + where.line());
    }

    private Operand storageOperand(Binding binding) {
        return binding.storage == Storage.GLOBAL ? Operand.global(binding.irName) : Operand.variable(binding.irName);
    }

    private String unique(String source) { return source + "$" + (++uniqueCounter); }
    private UnsupportedOperationException unsupported(String feature) { return new UnsupportedOperationException("IR/MIPS aun no soporta " + feature); }
}
