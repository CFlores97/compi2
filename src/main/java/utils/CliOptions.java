package utils;

public final class CliOptions {
    public String input;
    public String output = "output.s";
    public boolean emitAssembly;
    public boolean dumpIr;
    public boolean optimize;

    public static CliOptions parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Uso: archivo.mc [-S] [-o salida.s] [--dump-ir] [-O]");
        }
        if (args[0].startsWith("-")) throw new IllegalArgumentException("El primer argumento debe ser el archivo .mc.");
        CliOptions options = new CliOptions();
        options.input = args[0];
        for (int index = 1; index < args.length; index++) {
            switch (args[index]) {
                case "-S" -> options.emitAssembly = true;
                case "--dump-ir" -> options.dumpIr = true;
                case "-O" -> options.optimize = true;
                case "-o" -> {
                    if (++index >= args.length) throw new IllegalArgumentException("Falta el nombre de salida despues de -o.");
                    options.output = args[index];
                    options.emitAssembly = true;
                }
                default -> throw new IllegalArgumentException("Opcion no reconocida: " + args[index]);
            }
        }
        return options;
    }
}
