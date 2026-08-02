package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;

@Converter
public class ShopPhotoCategoryIdConverter implements AttributeConverter<ShopPhotoCategoryId, Long> {

    @Override
    public Long convertToDatabaseColumn(ShopPhotoCategoryId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ShopPhotoCategoryId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ShopPhotoCategoryId.of(dbData);
    }
}
