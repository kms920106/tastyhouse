package com.tastyhouse.core.domain.order.infrastructure.persistence.converter;

import com.tastyhouse.core.domain.order.domain.vo.OrderItemOptionId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderItemOptionIdConverter implements AttributeConverter<OrderItemOptionId, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderItemOptionId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public OrderItemOptionId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new OrderItemOptionId(dbData);
    }
}
