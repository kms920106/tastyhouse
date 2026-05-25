package com.tastyhouse.core.domain.order.infrastructure.persistence.converter;

import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderIdConverter implements AttributeConverter<OrderId, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public OrderId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new OrderId(dbData);
    }
}
