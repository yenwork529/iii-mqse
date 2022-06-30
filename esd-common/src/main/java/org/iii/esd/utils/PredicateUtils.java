package org.iii.esd.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PredicateUtils {
    private PredicateUtils() {}

    public static Predicate<BigDecimal> isGreaterThanOrEqual(BigDecimal base) {
        return (comp) -> comp.compareTo(base) >= 0;
    }

    public static Predicate<BigDecimal> isGreaterThan(BigDecimal base) {
        return (comp) -> comp.compareTo(base) > 0;
    }

    public static Predicate<BigDecimal> isLessThanOrEqual(BigDecimal base) {
        return (comp) -> comp.compareTo(base) <= 0;
    }

    public static Predicate<BigDecimal> isLessThan(BigDecimal base) {
        return (comp) -> comp.compareTo(base) < 0;
    }

    public static Predicate<BigDecimal> isEqual(BigDecimal base) {
        return (comp) -> comp.compareTo(base) == 0;
    }

    public static Predicate<Date> isEqual(Date base) {
        return (comp) -> comp.compareTo(base) == 0;
    }

    public static <T, U extends BigDecimal> Predicate<T> isGreaterThanZero(
            Function<T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (target) -> keyExtractor.apply(target)
                                       .compareTo(BigDecimal.ZERO) > 0;
    }

    public static <T, U extends BigDecimal> Predicate<T> isGreaterThanOrEqualZero(
            Function<T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (target) -> keyExtractor.apply(target)
                                       .compareTo(BigDecimal.ZERO) >= 0;
    }

    public static <T, U extends BigDecimal> Predicate<T> isLessThanZero(
            Function<T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (target) -> keyExtractor.apply(target)
                                       .compareTo(BigDecimal.ZERO) < 0;
    }

    public static <T, U extends BigDecimal> Predicate<T> isLessThanOrEqualZero(
            Function<T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (target) -> keyExtractor.apply(target)
                                       .compareTo(BigDecimal.ZERO) <= 0;
    }

    public static <T, U extends Object> Predicate<T> isExists(Function<T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (target) -> !Objects.isNull(keyExtractor.apply(target));
    }

    public static <T, U extends Object> Predicate<T> isEqualsTo(Object base, Function<T, ? extends U> keyExtractor) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(keyExtractor);

        return (target) ->
                !Objects.isNull(keyExtractor.apply(target))
                        && base.equals(keyExtractor.apply(target));
    }

    public static boolean isNotNull(Object target) {
        return !Objects.isNull(target);
    }

    public static <T, U extends Object> boolean isNotNull(Function<T, ? extends U> keyExtractor, T target) {
        return !Objects.isNull(keyExtractor.apply(target));
    }

    public static <T, U extends Date> Predicate<T> isBetween(Function<T, ? extends U> keyExtractor, Date start, Date end) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        return (target) -> isNotNull(keyExtractor, target)
                && start.getTime() <= keyExtractor.apply(target).getTime() // start.before(keyExtractor.apply(target))
                && end.getTime() >= keyExtractor.apply(target).getTime();  // end.after(keyExtractor.apply(target));
    }

    public static <T, U extends Date> Predicate<T> isBeforOrAt(Function<T, ? extends U> keyExtractor, Date date) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(date);

        return (target)->isNotNull(keyExtractor, target)
                && (keyExtractor.apply(target).getTime() <= date.getTime());
    }

    public static <T, U extends Date> Predicate<T> isAfterOrAt(Function<T, ? extends U> keyExtractor, Date date) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(date);

        return (target)->isNotNull(keyExtractor, target)
                && (keyExtractor.apply(target).getTime() >= date.getTime());
    }
}
