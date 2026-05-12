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
9. Opcionalmente imprimir tokens o parse tree.
*
* */


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {
        // 2. Validando que el ususario si mando la ruta
        if (args.length > 0) {

            // 1. obtenienido la ruta del archivo
            String userInput = args[0];

            try {

                //Archivo validado
                File sanitizedFile = validateFile(userInput);

                // Charstream a mandar al lexer
                String charStream = convertToCharStream(sanitizedFile);

            } catch (Exception e) {
                System.out.println("Ocurrio un error al validar archivo: " + e.getMessage());
            }


        } else {
            System.out.println("Ruta del archivo .mc no fue proporcionado");
        }
    }

    public static String convertToCharStream(File mcFile) throws IOException {

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