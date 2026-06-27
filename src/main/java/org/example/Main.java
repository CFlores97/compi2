package org.example;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.example.IR.ConstantFolder;
import org.example.IR.IRGenerator;
import org.example.IR.IRProgram;
import org.example.antlr.MiniCLexer;
import org.example.antlr.MiniCParser;
import org.example.ast.Ast;
import org.example.ast.MiniCASTBuilder;
import org.example.ast.RecorridoVisitor;
import org.example.mips.MipsEmitter;
import org.example.semantics.SemanticErrorReporter;
import org.example.semantics.SymbolTable;
import org.example.semantics.SymbolTableBuilder;
import org.example.semantics.TypeChecker;
import utils.CliOptions;
import utils.MinicErrorListener;
import utils.TreeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        try {
            CliOptions options = CliOptions.parse(args);
            File sourceFile = validateFile(options.input);
            String source = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);

            MinicErrorListener syntaxErrors = new MinicErrorListener();
            MiniCLexer lexer = new MiniCLexer(CharStreams.fromString(source));
            lexer.removeErrorListeners();
            lexer.addErrorListener(syntaxErrors);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MiniCParser parser = new MiniCParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(syntaxErrors);
            parser.setBuildParseTree(true);

            ParseTree tree = parser.program();
            if (syntaxErrors.hasErrors()) {
                System.err.println("El analisis termino con errores lexicos/sintacticos.");
                return;
            }

            System.out.println("Analisis sintactico exitoso.");

            Ast.Node ast = new MiniCASTBuilder().visit(tree);
            System.out.println("\n=== AST ===");
            System.out.println(ast.print(""));

            System.out.println("=== RECORRIDO DEL VISITOR ===");
            new RecorridoVisitor().visit(tree);

            SymbolTable symbolTable = new SymbolTable();
            SemanticErrorReporter semanticErrors = new SemanticErrorReporter();
            SymbolTableBuilder symbolBuilder = new SymbolTableBuilder(symbolTable, semanticErrors);


            symbolBuilder.collectFunctionSignatures((MiniCParser.ProgramContext) tree);
            ParseTreeWalker.DEFAULT.walk(symbolBuilder, tree);

            new TypeChecker(symbolTable, semanticErrors).check(ast);
            System.out.println(symbolTable);

            if (semanticErrors.hasErrors()) {
                System.err.println("El analisis semantico termino con errores.");
                semanticErrors.printErrors();
                System.err.println("No se genera IR ni MIPS por errores semanticos.");
                return;
            }
            System.out.println("Analisis semantico exitoso.");

            IRProgram ir = new IRGenerator(symbolTable).generate(ast);
            if (options.dumpIr) {
                System.out.println("\n=== IR ORIGINAL ===");
                System.out.println(ir);
            }

            if (options.optimize) {
                ir = new ConstantFolder().optimize(ir);
                if (options.dumpIr) {
                    System.out.println("\n=== IR OPTIMIZADO ===");
                    System.out.println(ir);
                }
            }

            if (options.emitAssembly) {
                String assembly = new MipsEmitter().emit(ir) + "\n\n# ===== RUNTIME =====\n\n" + loadRuntime();
                Path output = Path.of(options.output);
                if (output.getParent() != null) Files.createDirectories(output.getParent());
                Files.writeString(output, assembly, StandardCharsets.UTF_8);
                System.out.println("MIPS generado: " + output.toAbsolutePath());
            }

            System.out.println("\n=== PARSE TREE ===");
            System.out.println(TreeUtils.toPrettyTree(tree, Arrays.asList(parser.getRuleNames())));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ocurrio un error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static File validateFile(String route) throws Exception {
        File file = new File(route);
        if (!file.exists()) throw new Exception("La ruta proporcionada no existe.");
        if (file.isDirectory()) throw new Exception("La ruta no debe ser un directorio.");
        if (!file.getName().toLowerCase().endsWith(".mc")) throw new Exception("El archivo debe tener extension .mc.");
        return file;
    }

    private static String loadRuntime() throws IOException {
        try (InputStream input = Main.class.getResourceAsStream("/runtime/runtime.s")) {
            if (input == null) throw new FileNotFoundException("No se encontro runtime/runtime.s dentro de resources.");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
