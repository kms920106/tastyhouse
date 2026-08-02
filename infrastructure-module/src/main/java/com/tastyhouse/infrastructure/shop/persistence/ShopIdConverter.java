package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.shop.vo.ShopId;

@Converter
public class ShopIdConverter implements AttributeConverter<ShopId, Long> {

    @Override
    public Long convertToDatabaseColumn(ShopId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ShopId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ShopId.of(dbData);
    }
}
