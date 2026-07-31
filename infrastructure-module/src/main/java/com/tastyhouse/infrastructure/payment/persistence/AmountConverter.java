package com.tastyhouse.infrastructure.payment.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.payment.domain.vo.Amount;

@Converter(autoApply = true)
public class AmountConverter implements AttributeConverter<Amount, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Amount attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Amount convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : new Amount(dbData);
    }
}
