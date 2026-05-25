package com.tastyhouse.core.domain.payment.infrastructure.persistence.converter;

import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentIdConverter implements AttributeConverter<PaymentId, Long> {

    @Override
    public Long convertToDatabaseColumn(PaymentId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PaymentId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new PaymentId(dbData);
    }
}
