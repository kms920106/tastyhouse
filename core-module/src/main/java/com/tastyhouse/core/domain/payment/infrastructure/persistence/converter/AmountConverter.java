package com.tastyhouse.core.domain.payment.infrastructure.persistence.converter;

import com.tastyhouse.core.domain.payment.domain.vo.Amount;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
