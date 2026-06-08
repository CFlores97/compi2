package org.example;
/*
* LOGICA DEL main.java para manejar el archivo .mc
1. Recibir la ruta del archivo .mc desde la consola.
2. Validar que el usuario sí mandó una ruta.
3. Validar que el archivo exista.
4. Leer el contenido del archivo.
5. Pasar ese contenido al lexer de ANTLR.
6. Pasar los tokens del lexer al parser.
7. Ejecutar la regla inicial de la gramática, normalmente program.
8. Mostrar si hubo errores o si el análisis fue exitoso.
9. imprimir parse tree.
*
* */


import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import utils.TreeUtils;
import utils.MinicErrorListener;
import org.example.antlr.MiniCLexer;
import org.example.antlr.MiniCParser;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.example.semantics.SymbolTable;
import org.example.semantics.SymbolTableBuilder;
import org.example.semantics.SemanticErrorReporter;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {
    public static void main(String[] args) {
        // 2. Validando que el ususario si mando la ruta
        if (args.length > 0) {

            // 1. obtenienido la ruta del archivo
            String userInput = args[0];

            try {

                // Archivo validado
                File sanitizedFile = validateFile(userInput);

                // texto extraido del archivo .mc
                String textCode = extractMCFileText(sanitizedFile);

                // Inicializa el Charstream
                CharStream input = CharStreams.fromString(textCode);

                //Instancia compartida de error listener
                MinicErrorListener errorListener = new MinicErrorListener();

                // Mandarlo al lexer
                MiniCLexer lexer = new MiniCLexer(input);

                // agrega error personalizado a lexer
                lexer.removeErrorListeners();
                lexer.addErrorListener(errorListener);

                // Convierte en tokens para parser
                CommonTokenStream tokens = new CommonTokenStream(lexer);

                // Mandarlo al parser
                MiniCParser parser = new MiniCParser(tokens);

                // agrega error personalizado a parser
                parser.removeErrorListeners();
                parser.addErrorListener(errorListener);

                // Arbol de parseo
                parser.setBuildParseTree(true);

                // Aqui llama a la regla inicial
                ParseTree tree = parser.program();

                if (errorListener.hasErrors()) {
                    System.err.println("El analisis termino con errores.");
                } else {
                    System.out.println("Analisis sintactico exitoso");

                    SymbolTable symbolTable = new SymbolTable();
                    SemanticErrorReporter semanticErrors = new SemanticErrorReporter();
                    SymbolTableBuilder builder = new SymbolTableBuilder(symbolTable, semanticErrors);

                    ParseTreeWalker.DEFAULT.walk(builder, tree);

                    if (semanticErrors.hasErrors()) {
                        System.err.println("El analisis semantico termino con errores.");
                        semanticErrors.printErrors();
                    } else {
                        // Imprimir el árbol solo cuando todo está correcto
                        System.out.println("Analisis semantico exitoso.");
                        System.out.println("\nParse Tree:");
                        System.out.println(TreeUtils.toPrettyTree(tree, Arrays.asList(parser.getRuleNames())));
                    }

                    System.out.println(symbolTable);
                }


            } catch (Exception e) {
                System.err.println("Ocurrio un error: " + e.getMessage());
            }


        } else {
            System.err.println("Ruta del archivo .mc no fue proporcionado");
        }
    }

    public static String extractMCFileText(File mcFile) throws IOException {

        // Retorna el Charstream del .mc
        return Files.readString(mcFile.toPath(), StandardCharsets.UTF_8);
    }

    public static File validateFile(String ruta) throws Exception {

        // Archivo temporal
        File tempFile = new File(ruta);

        // Valida que el archivo que mando el usuario exista
        if (!tempFile.exists()) {
            throw new Exception("La ruta proporcionada no existe!");

        }

        // Valida que el archivo no sea un irectorio
        if (tempFile.isDirectory()) {

            throw new Exception("La ruta no debe ser la de un directorio!");

        }

        // Valida que el archivo sea un .mc
        if (tempFile.getName().toLowerCase().endsWith(".mc")) {
            return tempFile;
        }

        throw new Exception("Archivo debe ser de tipo: \'.mc\'");

    }
}