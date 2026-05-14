package utils;

import org.antlr.v4.runtime.*;

public class MinicErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);

        System.err.println("Error en la linea " + line + ", columna " + (charPositionInLine + 1) + ": " + msg);
    }
}
