package org.example.semantics;

// Reglas de compatibilidad de tipos entre Mini-C
public class TypeRules {

    // ¿Se puede asignar 'value' a una variable de tipo 'target'?
    public static boolean canAssign(MiniCType target, MiniCType value) {
        if (target == null || value == null) return false;

        // Mismo tipo exacto (incluye mismas dimensiones de arreglo)
        if (target.toString().equals(value.toString())) return true;

        // char se puede asignar a int (promoción), solo si ambos son simples
        if (target.getName().equals("int") && value.getName().equals("char")
                && target.getDimensions() == 0 && value.getDimensions() == 0) {
            return true;
        }

        return false;
    }

    // ¿Este tipo puede usarse como condición en if/while/for?
    public static boolean isConditionType(MiniCType type) {
        if (type == null) return false;
        // Debe ser un tipo simple (sin arreglo) y ser bool, int o char
        return type.getDimensions() == 0 &&
                (type.getName().equals("bool")
                        || type.getName().equals("int")
                        || type.getName().equals("char"));
    }

    // ¿Este tipo es un entero válido para usar como índice de arreglo?
    public static boolean isIndexType(MiniCType type) {
        if (type == null) return false;
        return type.getDimensions() == 0 &&
                (type.getName().equals("int") || type.getName().equals("char"));
    }
}