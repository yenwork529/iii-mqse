package org.iii.esd.utils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class Range<T> {

    private TypedPair<T> range;

    public Range(T begin, T end) {
        this.range = TypedPair.cons(begin, end);
    }

    public T getBegin() {
        return range.getLeft();
    }

    public T getEnd() {
        return range.getRight();
    }

    public TypedPair<T> getRange() {
        return range;
    }

    @Override
    public String toString() {
        return "Range{" +
                "begin=" + range.left() +
                ", end=" + range.right() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return !Objects.isNull(o)
                && (o instanceof Range)
                && range.equals(((Range<?>) o).range);
    }

    @Override
    public int hashCode() {
        return range.hashCode();
    }

    public List<T> toList() {
        return range.toList();
    }

    public Stream<T> stream() {
        return this.toList()
                   .stream();
    }

    public T map(Function<Range<T>, T> func) {
        return func.apply(this);
    }

    public T map(BiFunction<T, T, T> func) {
        return func.apply(this.range.left(), this.range.right());
    }

    public <U> U mapBegin(Function<T, U> func) {
        return func.apply(this.range.left());
    }

    public <U> U mapEnd(Function<T, U> func) {
        return func.apply(this.range.right());
    }

    public Range<T> lmap(Function<T, T> func) {
        return Range.from(mapBegin(func)).to(this.range.right());
    }

    public Range<T> rmap(Function<T, T> func) {
        return Range.from(this.range.left()).to(mapEnd(func));
    }

    public Range<T> biMap(Function<Range<T>, Range<T>> func) {
        return func.apply(this);
    }

    public Range<T> biMap(Function<T, T> lFunc, Function<T, T> rFunc) {
        return Range.from(lFunc.apply(this.range.left())).to(rFunc.apply(this.range.right()));
    }

    public static <T> RangeBuilder<T> from(T begin) {
        RangeBuilder<T> builder = new RangeBuilder<>();
        return builder.from(begin);
    }

    public static class RangeBuilder<T> {
        private T begin;
        private T end;

        private RangeBuilder() {}

        private RangeBuilder<T> from(T begin) {
            this.begin = begin;
            return this;
        }

        public Range<T> to(T end) {
            this.end = end;
            return build();
        }

        private Range<T> build() {
            return new Range<>(begin, end);
        }
    }
}
