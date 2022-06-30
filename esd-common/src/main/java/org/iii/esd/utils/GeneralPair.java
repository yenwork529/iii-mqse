package org.iii.esd.utils;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import lombok.Getter;

public class GeneralPair<L, R> {

    @Getter
    public final L left;

    @Getter
    public final R right;

    protected GeneralPair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static synchronized <L, R> GeneralPair<L, R> construct(L left, R right) {
        return new GeneralPair<>(left, right);
    }

    public L left() {
        return this.left;
    }

    public R right() {
        return this.right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        GeneralPair<?, ?> that = (GeneralPair<?, ?>) o;
        return left.equals(that.left) && right.equals(that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "GeneralPair{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }

    public <T> T map(Function<GeneralPair<L, R>, T> func) {
        return func.apply(this);
    }

    public <T> T map(BiFunction<L, R, T> func) {
        return func.apply(left, right);
    }

    public <U> U mapLeft(Function<L, U> func) {
        return func.apply(left);
    }

    public <U> U mapRight(Function<R, U> func) {
        return func.apply(right);
    }

    public <U> GeneralPair<U, R> lmap(Function<L, U> func) {
        return GeneralPair.construct(mapLeft(func), right);
    }

    public <U> GeneralPair<L, U> rmap(Function<R, U> func) {
        return GeneralPair.construct(left, mapRight(func));
    }

    public <T1, T2> GeneralPair<T1, T2> biMap(Function<GeneralPair<L, R>, GeneralPair<T1, T2>> func) {
        return func.apply(this);
    }

    public <T1, T2> GeneralPair<T1, T2> biMap(Function<L, T1> lFunc, Function<R, T2> rFunc) {
        return GeneralPair.construct(lFunc.apply(left), rFunc.apply(right));
    }
}
