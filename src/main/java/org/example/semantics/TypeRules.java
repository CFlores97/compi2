package org.example.semantics;

public final class TypeRules {

    private TypeRules() {
    }

    public static boolean sameType(
            MiniCType first,
            MiniCType second
    ) {
        if (first == null || second == null) {
            return false;
        }

        return first.getName().equals(second.getName())
                && first.isPointer() == second.isPointer()
                && first.getDimensions() == second.getDimensions();
    }

    public static boolean isScalar(MiniCType type) {
        return type != null
                && !type.isPointer()
                && type.getDimensions() == 0;
    }

    public static boolean isNumeric(MiniCType type) {
        return isScalar(type)
                && (type.getName().equals("int")
                || type.getName().equals("char"));
    }

    public static boolean isConditionType(MiniCType type) {
        return isScalar(type)
                && (type.getName().equals("bool")
                || type.getName().equals("int")
                || type.getName().equals("char"));
    }

    public static boolean isIndexType(MiniCType type) {
        return isNumeric(type);
    }

    public static boolean canAssign(
            MiniCType target,
            MiniCType value
    ) {
        if (target == null || value == null) {
            return false;
        }

        // No se permite asignar arreglos completos.
        if (target.getDimensions() > 0 ||
                value.getDimensions() > 0) {
            return false;
        }

        if (sameType(target, value)) {
            return true;
        }

        // Promoción permitida: char -> int.
        return !target.isPointer()
                && !value.isPointer()
                && target.getName().equals("int")
                && value.getName().equals("char");
    }

    public static boolean canCompareEquality(
            MiniCType left,
            MiniCType right
    ) {
        if (left == null || right == null) {
            return false;
        }

        if (sameType(left, right)) {
            return true;
        }

        return isNumeric(left) && isNumeric(right);
    }

    public static MiniCType arithmeticResult(
            MiniCType left,
            MiniCType right
    ) {
        if (!isNumeric(left) || !isNumeric(right)) {
            return null;
        }

        return new MiniCType("int", false, 0);
    }
}