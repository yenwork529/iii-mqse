package org.iii.esd.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.iii.esd.utils.DatetimeUtils.toDate;

public class PredicateUtilsTest {

    private static final BigDecimal LESS_ZERO = BigDecimal.valueOf(-10);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal GREATER_ZERO = BigDecimal.valueOf(10);

    @Test
    public void testIsGreaterThanZero() {
        List<TestDomain> testList = Arrays.asList(
                new TestDomain(LESS_ZERO),
                new TestDomain(ZERO),
                new TestDomain(GREATER_ZERO));

        List<TestDomain> results = testList.stream()
                                           .filter(PredicateUtils.isGreaterThanZero(TestDomain::getValue))
                                           .collect(Collectors.toList());

        Assertions.assertThat(results.isEmpty()).isFalse();
        Assertions.assertThat(results.get(0).getValue()).isEqualTo(GREATER_ZERO);
    }

    @Test
    public void testBetween() {
        LocalDate today = LocalDate.now();
        List<TemporalDomain> testList = Arrays.asList(
                new TemporalDomain(toDate(today.atTime(0, 0, 0))),
                new TemporalDomain(toDate(today.atTime(1, 0, 0))),
                new TemporalDomain(toDate(today.atTime(2, 0, 0))),
                new TemporalDomain(toDate(today.atTime(3, 0, 0)))
        );

        LocalDateTime start = today.atTime(1, 30, 0);
        LocalDateTime end = today.atTime(3, 30, 0);

        List<TemporalDomain> result = testList.stream()
                .filter(PredicateUtils.isBetween(TemporalDomain::getTime, toDate(start), toDate(end)))
                .collect(Collectors.toList());

        Assertions.assertThat(result).hasSize(2);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestDomain {
        private BigDecimal value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemporalDomain {
        private Date time;
    }
}
