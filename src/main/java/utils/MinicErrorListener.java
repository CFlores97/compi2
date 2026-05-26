package utils;

import org.antlr.v4.runtime.*;

public class MinicErrorListener extends BaseErrorListener {

    private int errorCount = 0;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);

        errorCount++;

        System.err.println("Error en la linea " + line + ", columna " + (charPositionInLine + 1) + ": " + msg);
    }

    public int gerErrorCount() {
        return errorCount;
    }

    public boolean hasErrors() {
        return errorCount > 0;
    }
}
