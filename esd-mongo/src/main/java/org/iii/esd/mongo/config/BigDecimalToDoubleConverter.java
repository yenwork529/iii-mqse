package org.iii.esd.mongo.config;

import java.math.BigDecimal;

import org.springframework.core.convert.converter.Converter;

public class BigDecimalToDoubleConverter implements Converter<BigDecimal, Double> {

    @Override
    public Double convert(BigDecimal source) {
        return source.doubleValue();
    }
}







