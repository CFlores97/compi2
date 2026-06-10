package org.example.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.example.antlr.MiniCBaseVisitor;
import org.example.antlr.MiniCParser;

public class RecorridoVisitor extends MiniCBaseVisitor<Void> {

    private int depth = 0;

    private String indent() {
        return "  ".repeat(depth);
    }

    @Override
    public Void visitChildren(RuleNode node) {
        if (node instanceof ParserRuleContext ctx) {
            String ruleName = MiniCParser.ruleNames[ctx.getRuleIndex()];

            System.out.println(indent() + "Visitando regla: " + ruleName);

            depth++;
            super.visitChildren(node);
            depth--;
        }

        return null;
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        String text = node.getText();

        if (!text.equals("<EOF>")) {
            System.out.println(indent() + "Token: " + text);
        }

        return null;
    }
}


