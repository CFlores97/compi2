package org.example.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public final class MiniCType {
    private final String name;
    private final int pointerDepth;
    private final List<Integer> dimensionSizes;

    public MiniCType(String name, boolean pointer, int dimensions) {
        this(name, pointer ? 1 : 0, unknownDimensions(dimensions));
    }

    public MiniCType(String name, int pointerDepth, List<Integer> dimensionSizes) {
        this.name = name;
        this.pointerDepth = Math.max(0, pointerDepth);
        this.dimensionSizes = Collections.unmodifiableList(new ArrayList<>(dimensionSizes));
    }

    public static MiniCType scalar(String name) {
        return new MiniCType(name, 0, List.of());
    }

    private static List<Integer> unknownDimensions(int count) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < count; i++) values.add(-1);
        return values;
    }

    public String getName() { return name; }
    public boolean isPointer() { return pointerDepth > 0; }
    public int getPointerDepth() { return pointerDepth; }
    public int getDimensions() { return dimensionSizes.size(); }
    public List<Integer> getDimensionSizes() { return dimensionSizes; }
    public boolean isArray() { return !dimensionSizes.isEmpty(); }

    /** All scalar backend values use one MIPS word. Arrays reserve product(dimensions)*4. */
    public int storageBytes() {
        if (!isArray()) return 4;
        long product = 1;
        for (Integer size : dimensionSizes) {
            if (size == null || size <= 0) return 4;
            product *= size;
        }
        long bytes = product * 4L;
        if (bytes > Integer.MAX_VALUE) throw new IllegalArgumentException("Arreglo demasiado grande");
        return (int) bytes;
    }

    public MiniCType elementTypeAfter(int indexes) {
        if (indexes < 0 || indexes > dimensionSizes.size()) {
            throw new IllegalArgumentException("Numero invalido de indices: " + indexes);
        }
        return new MiniCType(name, pointerDepth, dimensionSizes.subList(indexes, dimensionSizes.size()));
    }

    public MiniCType withPointerDepth(int newDepth) {
        return new MiniCType(name, newDepth, dimensionSizes);
    }

    public MiniCType addressType() {
        return new MiniCType(name, pointerDepth + 1, dimensionSizes);
    }

    public MiniCType dereferenceType() {
        if (pointerDepth <= 0) return null;
        return new MiniCType(name, pointerDepth - 1, dimensionSizes);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(name);
        result.append("*".repeat(pointerDepth));
        for (Integer size : dimensionSizes) {
            result.append("[");
            if (size != null && size > 0) result.append(size);
            result.append("]");
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MiniCType type)) return false;
        return name.equals(type.name)
                && pointerDepth == type.pointerDepth
                && dimensionSizes.equals(type.dimensionSizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pointerDepth, dimensionSizes);
    }
}
