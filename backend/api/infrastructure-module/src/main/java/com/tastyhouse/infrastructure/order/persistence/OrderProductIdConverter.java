package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.order.vo.OrderProductId;

@Converter
public class OrderProductIdConverter implements AttributeConverter<OrderProductId, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderProductId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public OrderProductId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : OrderProductId.of(dbData);
    }
}
