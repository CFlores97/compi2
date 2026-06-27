package org.example.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AST propio del compilador. A diferencia del Parse Tree, conserva solo la
 * estructura útil para semántica, IR y MIPS.
 */
public final class Ast {
    private Ast() { }

    public interface Node {
        int line();
        int column();
        String print(String indent);
    }

    public interface Expr extends Node { }

    public interface Stmt extends Node { }

    public interface LValue extends Expr { }

    private abstract static class BaseNode implements Node {
        public final int line;
        public final int column;

        protected BaseNode(int line, int column) {
            this.line = line;
            this.column = column;
        }

        @Override public int line() { return line; }
        @Override public int column() { return column; }
    }

    public static final class Program extends BaseNode {
        public final List<Node> items;

        public Program(int line, int column, List<Node> items) {
            super(line, column);
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("Program\n");
            for (Node item : items) sb.append(item.print(indent + "  "));
            return sb.toString();
        }
    }

    public static final class Function extends BaseNode {
        public final String returnType;
        public final String name;
        public final List<Param> params;
        public final Block body;

        public Function(int line, int column, String returnType, String name,
                        List<Param> params, Block body) {
            super(line, column);
            this.returnType = returnType;
            this.name = name;
            this.params = Collections.unmodifiableList(new ArrayList<>(params));
            this.body = body;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("Function ").append(name)
                    .append(" : ").append(returnType).append("\n");
            sb.append(indent).append("  Params\n");
            if (params.isEmpty()) sb.append(indent).append("    (sin parametros)\n");
            for (Param param : params) sb.append(param.print(indent + "    "));
            sb.append(indent).append("  Body\n");
            sb.append(body.print(indent + "    "));
            return sb.toString();
        }
    }

    public static final class Param extends BaseNode {
        public final String type;
        public final Declarator declarator;

        public Param(int line, int column, String type, Declarator declarator) {
            super(line, column);
            this.type = type;
            this.declarator = declarator;
        }

        @Override
        public String print(String indent) {
            return indent + "Param " + declarator.describe() + " : " + type + "\n";
        }
    }

    /** Declarator stores pointer depth, array dimensions and optional initializer. */
    public static final class Declarator extends BaseNode {
        public final String name;
        public final int pointerDepth;
        public final List<Integer> dimensions;
        public final Expr initializer;

        public Declarator(int line, int column, String name, int pointerDepth,
                          List<Integer> dimensions, Expr initializer) {
            super(line, column);
            this.name = name;
            this.pointerDepth = pointerDepth;
            this.dimensions = Collections.unmodifiableList(new ArrayList<>(dimensions));
            this.initializer = initializer;
        }

        public String describe() {
            StringBuilder sb = new StringBuilder();
            sb.append("*".repeat(Math.max(0, pointerDepth))).append(name);
            for (Integer size : dimensions) sb.append("[").append(size).append("]");
            return sb.toString();
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("Declarator ")
                    .append(describe()).append("\n");
            if (initializer != null) {
                sb.append(indent).append("  Initializer\n");
                sb.append(initializer.print(indent + "    "));
            }
            return sb.toString();
        }
    }

    public static final class Declaration extends BaseNode implements Stmt {
        public final String type;
        public final List<Declarator> declarators;

        public Declaration(int line, int column, String type, List<Declarator> declarators) {
            super(line, column);
            this.type = type;
            this.declarators = Collections.unmodifiableList(new ArrayList<>(declarators));
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("Declaration ")
                    .append(type).append("\n");
            for (Declarator declarator : declarators) sb.append(declarator.print(indent + "  "));
            return sb.toString();
        }
    }

    public static final class Block extends BaseNode implements Stmt {
        public final List<Node> items;

        public Block(int line, int column, List<Node> items) {
            super(line, column);
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("Block\n");
            if (items.isEmpty()) sb.append(indent).append("  (vacio)\n");
            for (Node item : items) sb.append(item.print(indent + "  "));
            return sb.toString();
        }
    }

    public static final class ReturnStmt extends BaseNode implements Stmt {
        public final Expr value;

        public ReturnStmt(int line, int column, Expr value) {
            super(line, column);
            this.value = value;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("Return\n");
            if (value != null) sb.append(value.print(indent + "  "));
            return sb.toString();
        }
    }

    public static final class AssignStmt extends BaseNode implements Stmt {
        public final LValue target;
        public final Expr value;

        public AssignStmt(int line, int column, LValue target, Expr value) {
            super(line, column);
            this.target = target;
            this.value = value;
        }

        @Override
        public String print(String indent) {
            return indent + "Assign\n" + target.print(indent + "  ")
                    + value.print(indent + "  ");
        }
    }

    public static final class ExprStmt extends BaseNode implements Stmt {
        public final Expr expr;

        public ExprStmt(int line, int column, Expr expr) {
            super(line, column);
            this.expr = expr;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("ExprStmt\n");
            if (expr != null) sb.append(expr.print(indent + "  "));
            return sb.toString();
        }
    }

    public static final class IfStmt extends BaseNode implements Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public IfStmt(int line, int column, Expr condition, Stmt thenBranch, Stmt elseBranch) {
            super(line, column);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("If\n");
            sb.append(indent).append("  Condition\n").append(condition.print(indent + "    "));
            sb.append(indent).append("  Then\n").append(thenBranch.print(indent + "    "));
            if (elseBranch != null) sb.append(indent).append("  Else\n").append(elseBranch.print(indent + "    "));
            return sb.toString();
        }
    }

    public static final class WhileStmt extends BaseNode implements Stmt {
        public final Expr condition;
        public final Stmt body;

        public WhileStmt(int line, int column, Expr condition, Stmt body) {
            super(line, column);
            this.condition = condition;
            this.body = body;
        }

        @Override
        public String print(String indent) {
            return indent + "While\n" + condition.print(indent + "  ") + body.print(indent + "  ");
        }
    }

    public static final class ForStmt extends BaseNode implements Stmt {
        public final Stmt init;
        public final Expr condition;
        public final Stmt step;
        public final Stmt body;

        public ForStmt(int line, int column, Stmt init, Expr condition, Stmt step, Stmt body) {
            super(line, column);
            this.init = init;
            this.condition = condition;
            this.step = step;
            this.body = body;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("For\n");
            if (init != null) sb.append(indent).append("  Init\n").append(init.print(indent + "    "));
            if (condition != null) sb.append(indent).append("  Condition\n").append(condition.print(indent + "    "));
            if (step != null) sb.append(indent).append("  Step\n").append(step.print(indent + "    "));
            sb.append(indent).append("  Body\n").append(body.print(indent + "    "));
            return sb.toString();
        }
    }

    public static final class DoWhileStmt extends BaseNode implements Stmt {
        public final Stmt body;
        public final Expr condition;

        public DoWhileStmt(int line, int column, Stmt body, Expr condition) {
            super(line, column);
            this.body = body;
            this.condition = condition;
        }

        @Override
        public String print(String indent) {
            return indent + "DoWhile\n" + body.print(indent + "  ")
                    + condition.print(indent + "  ");
        }
    }

    public static final class VariableLValue extends BaseNode implements LValue {
        public final String name;

        public VariableLValue(int line, int column, String name) {
            super(line, column);
            this.name = name;
        }

        @Override public String print(String indent) { return indent + "Variable " + name + "\n"; }
    }

    public static final class ArrayLValue extends BaseNode implements LValue {
        public final String name;
        public final List<Expr> indices;

        public ArrayLValue(int line, int column, String name, List<Expr> indices) {
            super(line, column);
            this.name = name;
            this.indices = Collections.unmodifiableList(new ArrayList<>(indices));
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("ArrayAccess ").append(name).append("\n");
            for (Expr index : indices) sb.append(index.print(indent + "  "));
            return sb.toString();
        }
    }

    public static final class DerefLValue extends BaseNode implements LValue {
        public final Expr pointer;

        public DerefLValue(int line, int column, Expr pointer) {
            super(line, column);
            this.pointer = pointer;
        }

        @Override public String print(String indent) { return indent + "Deref\n" + pointer.print(indent + "  "); }
    }

    public enum LiteralKind { INT, CHAR, BOOL, STRING }

    public static final class Literal extends BaseNode implements Expr {
        public final LiteralKind kind;
        public final String value;

        public Literal(int line, int column, LiteralKind kind, String value) {
            super(line, column);
            this.kind = kind;
            this.value = value;
        }

        @Override public String print(String indent) { return indent + "Literal " + value + "\n"; }
    }

    public static final class BinaryExpr extends BaseNode implements Expr {
        public final Expr left;
        public final String op;
        public final Expr right;

        public BinaryExpr(int line, int column, Expr left, String op, Expr right) {
            super(line, column);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public String print(String indent) {
            return indent + "Binary " + op + "\n" + left.print(indent + "  ") + right.print(indent + "  ");
        }
    }

    public static final class UnaryExpr extends BaseNode implements Expr {
        public final String op;
        public final Expr expr;

        public UnaryExpr(int line, int column, String op, Expr expr) {
            super(line, column);
            this.op = op;
            this.expr = expr;
        }

        @Override public String print(String indent) { return indent + "Unary " + op + "\n" + expr.print(indent + "  "); }
    }

    public static final class Call extends BaseNode implements Expr {
        public final String name;
        public final List<Expr> args;

        public Call(int line, int column, String name, List<Expr> args) {
            super(line, column);
            this.name = name;
            this.args = Collections.unmodifiableList(new ArrayList<>(args));
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder(indent).append("Call ").append(name).append("\n");
            for (Expr arg : args) sb.append(arg.print(indent + "  "));
            return sb.toString();
        }
    }
}
