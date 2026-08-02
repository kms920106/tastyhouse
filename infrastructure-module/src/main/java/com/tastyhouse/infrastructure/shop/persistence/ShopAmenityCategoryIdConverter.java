package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;

@Converter
public class ShopAmenityCategoryIdConverter implements AttributeConverter<ShopAmenityCategoryId, Long> {

    @Override
    public Long convertToDatabaseColumn(ShopAmenityCategoryId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ShopAmenityCategoryId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ShopAmenityCategoryId.of(dbData);
    }
}
