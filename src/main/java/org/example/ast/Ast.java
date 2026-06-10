package org.example.ast;


import java.util.List;
import java.util.ArrayList;

public final class Ast {
    private Ast() {
    }

    public interface Node {
        String print(String indent);
    }

    public interface Expr extends Node {
    }

    public static class Program implements Node {
        private final List<Node> items;

        public Program(List<Node> items) {
            this.items = items;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("Program\n");
            for (Node item : items) {
                sb.append(item.print(indent + " "));
            }
            return sb.toString();
        }
    }

    public static class Function implements Node {
        private final String returnType;
        private final String name;
        private final List<Param> params;
        private final Block body;

        public Function(String returnType, String name, List<Param> params, Block body) {
            this.returnType = returnType;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent)
                    .append("Function ")
                    .append(name)
                    .append(" : ")
                    .append(returnType)
                    .append("\n");
            sb.append(indent).append(" Params\n");
            if (params.isEmpty()) {
                sb.append(indent).append(" (sin parametros)\n");
            } else {
                for (Param param : params) {
                    sb.append(param.print(indent + " "));
                }
            }
            sb.append(indent).append(" Body\n");
            sb.append(body.print(indent + " "));
            return sb.toString();
        }
    }

    public static class Param implements Node {
        private final String type;
        private final String name;

        public Param(String type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String print(String indent) {
            return indent + "Param " + name + " : " + type + "\n";
        }
    }

    public static class Block implements Node {
        private final List<Node> items;

        public Block(List<Node> items) {
            this.items = items;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("Block\n");
            if (items.isEmpty()) {
                sb.append(indent).append(" (vacio)\n");
            } else {
                for (Node item : items) {
                    sb.append(item.print(indent + " "));
                }
            }
            return sb.toString();
        }
    }

    public static class Declaration implements Node {
        private final String type;
        private final List<String> names;

        public Declaration(String type, List<String> names) {
            this.type = type;
            this.names = names;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("Declaration ").append(type).append("\n");
            for (String name : names) {
                sb.append(indent).append(" ").append(name).append("\n");
            }
            return sb.toString();
        }
    }

    public static class ReturnStmt implements Node {
        private final Expr value;

        public ReturnStmt(Expr value) {
            this.value = value;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("Return\n");
            if (value != null) {
                sb.append(value.print(indent + " "));
            }
            return sb.toString();
        }
    }

    public static class AssignStmt implements Node {
        private final String target;
        private final Expr value;

        public AssignStmt(String target, Expr value) {

            this.target = target;
            this.value = value;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("Assign\n");
            sb.append(indent).append(" Target: ").append(target).append("\n");
            sb.append(indent).append(" Value\n");
            sb.append(value.print(indent + " "));
            return sb.toString();
        }
    }

    public static class IfStmt implements Node {
        private final Expr condition;
        private final Node thenBranch;
        private final Node elseBranch;

        public IfStmt(Expr condition, Node thenBranch, Node elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("If\n");
            sb.append(indent).append(" Condition\n");
            sb.append(condition.print(indent + " "));
            sb.append(indent).append(" Then\n");
            sb.append(thenBranch.print(indent + " "));
            if (elseBranch != null) {
                sb.append(indent).append(" Else\n");
                sb.append(elseBranch.print(indent + " "));
            }
            return sb.toString();
        }
    }

    public static class WhileStmt implements Node {
        private final Expr condition;
        private final Node body;

        public WhileStmt(Expr condition, Node body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("While\n");
            sb.append(indent).append(" Condition\n");
            sb.append(condition.print(indent + " "));
            sb.append(indent).append(" Body\n");
            sb.append(body.print(indent + " "));
            return sb.toString();
        }
    }

    public static class ExprStmt implements Node {
        private final Expr expr;

        public ExprStmt(Expr expr) {
            this.expr = expr;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("ExprStmt\n");
            if (expr != null) {
                sb.append(expr.print(indent + " "));
            }
            return sb.toString();
        }
    }

    public static class RawStmt implements Node {
        private final String text;

        public RawStmt(String text) {
            this.text = text;
        }

        @Override
        public String print(String indent) {
            return indent + "Stmt " + text + "\n";
        }
    }

    public static class RawExpr implements Expr {
        private final String text;

        public RawExpr(String text) {
            this.text = text;
        }

        @Override
        public String print(String indent) {
            return indent + "Expr " + text + "\n";
        }
    }

    public static class Variable implements Expr {
        private final String name;

        public Variable(String name) {
            this.name = name;
        }

        @Override
        public String print(String indent) {
            return indent + "Variable " + name + "\n";
        }
    }

    public static class Call implements Expr {
        private final String name;
        private final List<Expr> args;

        public Call(String name, List<Expr> args) {
            this.name = name;
            this.args = args;
        }

        @Override
        public String print(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indent).append("Call ").append(name).append("\n");
            if (args.isEmpty()) {
                sb.append(indent).append(" Args: (sin argumentos)\n");
            } else {
                sb.append(indent).append(" Args\n");
                for (Expr arg : args) {
                    sb.append(arg.print(indent + " "));
                }
            }
            return sb.toString();
        }
    }

    public static List<Node> nodeList() {
        return new ArrayList<>();
    }

}
