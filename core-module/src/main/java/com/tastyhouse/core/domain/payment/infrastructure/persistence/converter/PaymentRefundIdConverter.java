package com.tastyhouse.core.domain.payment.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.core.domain.payment.domain.vo.PaymentRefundId;

@Converter(autoApply = true)
public class PaymentRefundIdConverter implements AttributeConverter<PaymentRefundId, Long> {

    @Override
    public Long convertToDatabaseColumn(PaymentRefundId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PaymentRefundId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : PaymentRefundId.of(dbData);
    }
}
