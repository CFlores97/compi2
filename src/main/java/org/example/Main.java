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
import org.example.IR.IRGenerator;
import org.example.IR.IRProgram;
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
import org.example.ast.Ast;
import org.example.ast.MiniCASTBuilder;
import org.example.ast.RecorridoVisitor;

import org.example.semantics.TypeChecker;

import org.example.IR.ConstantFolder;
import org.example.IR.IRGenerator;
import org.example.IR.IRProgram;
import org.example.mips.MipsEmitter;
import utils.CliOptions;

import java.io.InputStream;
import java.nio.file.Path;


public class Main {
    public static void main(String[] args) {
        // 2. Validando que el ususario si mando la ruta
        CliOptions options;

        try {
            options = CliOptions.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        try {

            // Archivo validado
            File sanitizedFile = validateFile(options.input);

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


                MiniCASTBuilder astBuilder = new MiniCASTBuilder();
                Ast.Node ast = astBuilder.visit(tree);

                // impreison de AST
                System.out.println(" == AST == ");
                System.out.println(ast.print(""));

                // impresion de recorrido de visitor
                // esto para validar que en ANTLR visita los nodos
                System.out.println(" == Recorrido del visitor == ");
                RecorridoVisitor trace = new RecorridoVisitor();
                trace.visit(tree);


                SymbolTable symbolTable = new SymbolTable();
                SemanticErrorReporter semanticErrors = new SemanticErrorReporter();
                SymbolTableBuilder builder = new SymbolTableBuilder(symbolTable, semanticErrors);

                ParseTreeWalker.DEFAULT.walk(builder, tree);

                // Ejecutar el TypeChecker sobre el AST
                TypeChecker typeChecker = new TypeChecker(symbolTable, semanticErrors);
                typeChecker.check(ast);

                // impresion de tabla de simbolos
                System.out.println(symbolTable);

                if (semanticErrors.hasErrors()) {
                    System.err.println("El analisis semantico termino con errores.");
                    semanticErrors.printErrors();

                    System.out.println("No se genera IR  ni MIPS por errores semanticos.");
                    return;
                }

                System.out.println("Analisis semantico exitoso.");

                // Genera el IR
                IRGenerator irGenerator = new IRGenerator();
                IRProgram irProgram = irGenerator.generate(ast);


                if (options.dumpIr) {
                    System.out.println("\n === IR ORIGINAL ===");
                    System.out.println(irProgram);
                }

                if(options.optimize) {
                    irProgram = new ConstantFolder().optimize(irProgram);

                    if(options.dumpIr) {
                        System.out.println("\n=== IR OPTIMIZADO ===");
                        System.out.println(irProgram);
                    }
                }

                if(options.emitAssembly) {
                    String asm = new MipsEmitter().emit(irProgram);

                    String runtime = loadRuntime();

                    Path outputPath = Path.of(options.output);

                    if(outputPath.getParent() != null) {
                        Files.createDirectories(outputPath.getParent());
                    }

                    Files.writeString(
                            outputPath,
                            asm + "\n\n# === RUNTIME ===\n\n" + runtime,
                            StandardCharsets.UTF_8
                    );

                    System.out.println("MIPS generado: " + options.output);
                }


                // impresion de arbol de parseo
                List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
                String prettyTree = TreeUtils.toPrettyTree(tree, ruleNamesList);
                System.out.println(" == Parse Tree == ");
                System.out.println(prettyTree);
            }


        } catch (Exception e) {
            System.err.println("Ocurrio un error: " + e.getMessage());
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

    public static String loadRuntime() throws IOException {
        try (InputStream input = Main.class.getResourceAsStream("/runtime/runtime.s")){
            if (input == null) {
                throw new FileNotFoundException("No se encontro el runtime.s dentro de resources!");
            }

            return new String(input.readAllBytes(), StandardCharsets.UTF_8);

        }
    }
}