package com.tastyhouse.infrastructure.payment.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

@Converter(autoApply = true)
public class PaymentIdConverter implements AttributeConverter<PaymentId, Long> {

    @Override
    public Long convertToDatabaseColumn(PaymentId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PaymentId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : PaymentId.of(dbData);
    }
}
