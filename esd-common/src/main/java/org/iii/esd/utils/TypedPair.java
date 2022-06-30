package org.iii.esd.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TypedPair<T> extends GeneralPair<T, T> {

    public TypedPair(T left, T right) {
        super(left, right);
    }

    public static synchronized <T> TypedPair<T> cons(T left, T right) {
        return new TypedPair<>(left, right);
    }

    @Override
    public String toString() {
        return "TypedPair{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }

    public List<T> toList() {
        return Collections.unmodifiableList(Arrays.asList(left, right));
    }

    public Stream<T> stream() {
        return this.toList()
                   .stream();
    }

    @Override
    public boolean equals(Object o) {
        return !Objects.isNull(o)
                && (o instanceof TypedPair)
                && Objects.equals(((TypedPair) o).left(), this.left)
                && Objects.equals(((TypedPair) o).right(), this.right);
    }

    @Override
    public int hashCode() {
        return (this.left.hashCode() + this.right.hashCode()) * 47 + 4723;
    }
}
