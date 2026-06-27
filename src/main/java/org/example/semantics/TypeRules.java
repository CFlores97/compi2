package org.example.semantics;

public final class TypeRules {
    private TypeRules() { }

    public static boolean sameType(MiniCType left, MiniCType right) {
        return left != null && left.equals(right);
    }

    public static boolean isScalar(MiniCType type) {
        return type != null && !type.isArray();
    }

    public static boolean isNumeric(MiniCType type) {
        return isScalar(type) && !type.isPointer()
                && ("int".equals(type.getName()) || "char".equals(type.getName()));
    }

    public static boolean isConditionType(MiniCType type) {
        return isScalar(type) && !type.isPointer()
                && ("bool".equals(type.getName()) || "int".equals(type.getName()) || "char".equals(type.getName()));
    }

    public static boolean isIndexType(MiniCType type) {
        return isNumeric(type);
    }

    public static boolean canAssign(MiniCType target, MiniCType value) {
        if (target == null || value == null) return false;
        // Arrays do not receive whole-array assignment in this Mini-C implementation.
        if (target.isArray() || value.isArray()) return false;
        if (sameType(target, value)) return true;
        // Safe numeric promotion required by the assignment specification.
        return "int".equals(target.getName()) && target.getPointerDepth() == 0
                && "char".equals(value.getName()) && value.getPointerDepth() == 0;
    }

    public static boolean canCompareEquality(MiniCType left, MiniCType right) {
        if (left == null || right == null) return false;
        return sameType(left, right) || (isNumeric(left) && isNumeric(right));
    }

    public static MiniCType arithmeticResult(MiniCType left, MiniCType right) {
        if (!isNumeric(left) || !isNumeric(right)) return null;
        return MiniCType.scalar("int");
    }
}
