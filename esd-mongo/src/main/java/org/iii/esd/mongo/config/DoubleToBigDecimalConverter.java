package org.iii.esd.mongo.config;

import java.math.BigDecimal;

import org.springframework.core.convert.converter.Converter;

public class DoubleToBigDecimalConverter implements Converter<Double, BigDecimal> {

    @Override
    public BigDecimal convert(Double source) {
        return new BigDecimal(source.toString());
    }
}

