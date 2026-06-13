package com.tastyhouse.core.domain.order.infrastructure.persistence.converter;

import com.tastyhouse.core.domain.order.domain.vo.OrderProductOptionId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderProductOptionIdConverter implements AttributeConverter<OrderProductOptionId, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderProductOptionId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public OrderProductOptionId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new OrderProductOptionId(dbData);
    }
}
