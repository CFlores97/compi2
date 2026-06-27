package utils;

public class CliOptions {

    public String input;
    public String output = "output.s";

    public boolean emitAssembly = false;
    public boolean dumpIr = false;
    public boolean optimize = false;

    public static CliOptions parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException(
                    "Uso: archivo.mc [-S] [-o salida.s] [--dump-ir] [-O]"
            );
        }

        if (args[0].startsWith("-")) {
            throw new IllegalArgumentException(
                    "El primer argumento debe ser el archivo .mc."
            );
        }

        CliOptions options = new CliOptions();
        options.input = args[0];

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {

                case "-S" -> options.emitAssembly = true;

                case "--dump-ir" -> options.dumpIr = true;

                case "-O" -> options.optimize = true;

                case "-o" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException(
                                "Falta el nombre del archivo después de -o."
                        );
                    }

                    options.output = args[++i];

                    // Si el usuario indica salida, asumimos que quiere generar .s.
                    options.emitAssembly = true;
                }

                default -> throw new IllegalArgumentException(
                        "Opción no reconocida: " + args[i]
                );
            }
        }

        return options;
    }
}