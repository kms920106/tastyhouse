package com.tastyhouse.core.domain.order.infrastructure.persistence.converter;

import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderProductIdConverter implements AttributeConverter<OrderProductId, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderProductId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public OrderProductId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new OrderProductId(dbData);
    }
}
