package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.shop.domain.vo.ShopFoodTypeCategoryId;

@Converter
public class ShopFoodTypeCategoryIdConverter implements AttributeConverter<ShopFoodTypeCategoryId, Long> {

    @Override
    public Long convertToDatabaseColumn(ShopFoodTypeCategoryId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ShopFoodTypeCategoryId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ShopFoodTypeCategoryId.of(dbData);
    }
}
