package org.example.IR;

import org.example.ast.Ast;

public class IRGenerator {

    private final TempFactory temps = new TempFactory();
    private final LabelFactory labels = new LabelFactory();

    private IRProgram program;
    private IRFunction current;

    public IRProgram generate(Ast.Node ast) {
        if (!(ast instanceof Ast.Program root)) {
            throw new IllegalArgumentException(
                    "IRGenerator espera Ast.Program como nodo raiz."
            );
        }

        temps.reset();
        labels.reset();

        program = new IRProgram();

        for (Ast.Node item : root.items) {
            if (item instanceof Ast.Function function) {
                genFunction(function);

            } else if (item instanceof Ast.Declaration) {
                throw unsupported("variables globales");

            } else {
                throw new UnsupportedOperationException(
                        "Nodo global no soportado en IR: "
                                + item.getClass().getSimpleName()
                );
            }
        }

        return program;
    }

    private void genFunction(Ast.Function function) {
        current = new IRFunction(function.name);

        for (Ast.Param param : function.params) {
            ensureSimpleDeclarator(param.name, "parametro");
            current.addParam(param.name);
        }

        genBlock(function.body);

        program.addFunction(current);
        current = null;
    }

    private void genBlock(Ast.Block block) {
        for (Ast.Node item : block.items) {
            genStmt(item);
        }
    }

    private void genStmt(Ast.Node node) {
        if (node == null) {
            return;
        }

        if (node instanceof Ast.Block block) {
            genBlock(block);

        } else if (node instanceof Ast.Declaration declaration) {
            genDeclaration(declaration);

        } else if (node instanceof Ast.AssignStmt assign) {
            genAssign(assign);

        } else if (node instanceof Ast.ReturnStmt returnStmt) {
            genReturn(returnStmt);

        } else if (node instanceof Ast.IfStmt ifStmt) {
            genIf(ifStmt);

        } else if (node instanceof Ast.WhileStmt whileStmt) {
            genWhile(whileStmt);

        } else if (node instanceof Ast.ExprStmt exprStmt) {
            genExprStmt(exprStmt);

        } else if (node instanceof Ast.RawStmt) {
            throw unsupported(
                    "for o do-while; el AST actual los representa como RawStmt"
            );

        } else {
            throw new UnsupportedOperationException(
                    "Sentencia no soportada en IR: "
                            + node.getClass().getSimpleName()
            );
        }
    }

    private void genDeclaration(Ast.Declaration declaration) {
        for (String name : declaration.names) {
            ensureSimpleDeclarator(name, "declaracion local");
            current.addLocal(name);
        }
    }

    private void genAssign(Ast.AssignStmt assign) {
        ensureSimpleDeclarator(assign.target, "destino de asignacion");

        Operand value = genExpr(assign.value);

        current.emit(
                IRInstruction.assign(
                        Operand.variable(assign.target),
                        value
                )
        );
    }

    private void genReturn(Ast.ReturnStmt returnStmt) {
        Operand value = returnStmt.value == null
                ? null
                : genExpr(returnStmt.value);

        current.emit(IRInstruction.ret(value));
    }

    private void genExprStmt(Ast.ExprStmt exprStmt) {
        if (exprStmt.expr == null) {
            return;
        }

        if (exprStmt.expr instanceof Ast.Call call) {
            genCall(call, false);
            return;
        }

        // Se evalúa aunque su resultado no sea usado.
        genExpr(exprStmt.expr);
    }

    private Operand genExpr(Ast.Expr expr) {
        if (expr == null) {
            throw new IllegalArgumentException(
                    "No se puede generar IR de una expresion nula."
            );
        }

        if (expr instanceof Ast.Literal literal) {
            return genLiteral(literal);
        }

        if (expr instanceof Ast.Variable variable) {
            ensureSimpleDeclarator(variable.name, "uso de variable");
            return Operand.variable(variable.name);
        }

        if (expr instanceof Ast.BinaryExpr binary) {
            return genBinary(binary);
        }

        if (expr instanceof Ast.UnaryExpr unary) {
            return genUnary(unary);
        }

        if (expr instanceof Ast.Call call) {
            return genCall(call, true);
        }

        if (expr instanceof Ast.RawExpr) {
            throw unsupported(
                    "RawExpr; ajusta MiniCASTBuilder "
                            + "para producir nodos reales"
            );
        }

        throw new UnsupportedOperationException(
                "Expresion no soportada en IR: "
                        + expr.getClass().getSimpleName()
        );
    }

    private Operand genLiteral(Ast.Literal literal) {
        String value = literal.value;

        if ("true".equals(value)) {
            return Operand.constant("1");
        }

        if ("false".equals(value)) {
            return Operand.constant("0");
        }

        if (value.startsWith("\"") && value.endsWith("\"")) {
            throw unsupported(
                    "strings; aun requieren generacion de datos en .data"
            );
        }

        if (value.startsWith("'") && value.endsWith("'")) {
            return Operand.constant(
                    String.valueOf(parseCharLiteral(value))
            );
        }

        return Operand.constant(value);
    }

    private Operand genBinary(Ast.BinaryExpr binary) {
        Operand left = genExpr(binary.left);
        Operand right = genExpr(binary.right);

        Operand result = temps.newTemp();

        current.emit(
                IRInstruction.bin(
                        opFromString(binary.op),
                        result,
                        left,
                        right
                )
        );

        return result;
    }

    private Operand genUnary(Ast.UnaryExpr unary) {
        Operand value = genExpr(unary.expr);
        Operand result = temps.newTemp();

        IROp operation = switch (unary.op) {
            case "-" -> IROp.NEG;
            case "!" -> IROp.NOT;
            case "*", "&" -> throw unsupported(
                    "punteros y direccion de memoria"
            );
            default -> throw new IllegalArgumentException(
                    "Operador unario no soportado: " + unary.op
            );
        };

        current.emit(
                IRInstruction.unary(operation, result, value)
        );

        return result;
    }

    private Operand genCall(Ast.Call call, boolean needsResult) {
        for (Ast.Expr arg : call.args) {
            current.emit(
                    IRInstruction.param(genExpr(arg))
            );
        }

        Operand result = needsResult
                ? temps.newTemp()
                : null;

        current.emit(
                IRInstruction.call(
                        result,
                        Operand.function(call.name),
                        call.args.size()
                )
        );

        return result;
    }

    private void genIf(Ast.IfStmt ifStmt) {
        Operand condition = genExpr(ifStmt.condition);

        if (ifStmt.elseBranch == null) {
            Operand end = labels.newLabel("if_end_");

            current.emit(IRInstruction.ifz(condition, end));

            genStmt(ifStmt.thenBranch);

            current.emit(IRInstruction.label(end));

            return;
        }

        Operand elseLabel = labels.newLabel("if_else_");
        Operand endLabel = labels.newLabel("if_end_");

        current.emit(IRInstruction.ifz(condition, elseLabel));

        genStmt(ifStmt.thenBranch);

        current.emit(IRInstruction.goTo(endLabel));

        current.emit(IRInstruction.label(elseLabel));

        genStmt(ifStmt.elseBranch);

        current.emit(IRInstruction.label(endLabel));
    }

    private void genWhile(Ast.WhileStmt whileStmt) {
        Operand startLabel = labels.newLabel("while_start_");
        Operand endLabel = labels.newLabel("while_end_");

        current.emit(IRInstruction.label(startLabel));

        Operand condition = genExpr(whileStmt.condition);

        current.emit(IRInstruction.ifz(condition, endLabel));

        genStmt(whileStmt.body);

        current.emit(IRInstruction.goTo(startLabel));

        current.emit(IRInstruction.label(endLabel));
    }

    private IROp opFromString(String op) {
        return switch (op) {
            case "+" -> IROp.ADD;
            case "-" -> IROp.SUB;
            case "*" -> IROp.MUL;
            case "/" -> IROp.DIV;
            case "%" -> IROp.MOD;

            case "<" -> IROp.LT;
            case "<=" -> IROp.LE;
            case ">" -> IROp.GT;
            case ">=" -> IROp.GE;

            case "==" -> IROp.EQ;
            case "!=" -> IROp.NE;

            case "&&" -> IROp.AND;
            case "||" -> IROp.OR;

            default -> throw new IllegalArgumentException(
                    "Operador no soportado en IR: " + op
            );
        };
    }

    private void ensureSimpleDeclarator(
            String name,
            String context
    ) {
        if (name.startsWith("*") || name.contains("[")) {
            throw unsupported(
                    context + " con punteros o arreglos: " + name
            );
        }
    }

    private int parseCharLiteral(String text) {
        String content = text.substring(1, text.length() - 1);

        if (content.length() == 1) {
            return content.charAt(0);
        }

        return switch (content) {
            case "\\n" -> '\n';
            case "\\t" -> '\t';
            case "\\r" -> '\r';
            case "\\0" -> 0;
            case "\\\\" -> '\\';
            case "\\'" -> '\'';
            default -> throw new IllegalArgumentException(
                    "Literal char no soportado: " + text
            );
        };
    }

    private UnsupportedOperationException unsupported(String feature) {
        return new UnsupportedOperationException(
                "IR/MIPS aun no soporta " + feature + ". "
                        + "Usa int, bool, char, asignaciones, "
                        + "expresiones, if, while, llamadas y return."
        );
    }
}