package org.iii.esd.utils;

import java.util.Optional;

public class OptionalPair<L, R> {
    public static synchronized <L, R> OptionalPair<L, R> byLeft(L left, Class<R> rClaz) {
        OptionalPair<L, R> pair = new OptionalPair<>();
        pair.setLeft(left);
        return pair;
    }

    public static synchronized <L, R> OptionalPair<L, R> byRight(R right, Class<L> rClaz) {
        OptionalPair<L, R> pair = new OptionalPair<>();
        pair.setRight(right);
        return pair;
    }

    public static synchronized <L, R> OptionalPair<L, R> cons(L left, R right) {
        OptionalPair<L, R> pair = new OptionalPair<>(left, right);
        return pair;
    }

    private Optional<L> left = Optional.empty();
    private Optional<R> right = Optional.empty();

    public OptionalPair(L left, R right) {
        this.left = Optional.ofNullable(left);
        this.right = Optional.ofNullable(right);
    }

    public OptionalPair() {}

    public void setLeft(L left) {
        this.left = Optional.of(left);
    }

    public void setRight(R right) {
        this.right = Optional.of(right);
    }

    public L getLeft() {
        return this.left.get();
    }

    public R getRight() {
        return this.right.get();
    }

    public boolean hasLeft() {
        return this.left.isPresent();
    }

    public boolean hasRight() {
        return this.right.isPresent();
    }
}
