package com.tastyhouse.core.domain.order.infrastructure.persistence.converter;

import com.tastyhouse.core.domain.order.domain.vo.OrderItemId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderItemIdConverter implements AttributeConverter<OrderItemId, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderItemId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public OrderItemId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new OrderItemId(dbData);
    }
}
