package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.product.domain.vo.BbqCategoryId;

@Converter
public class BbqCategoryIdConverter implements AttributeConverter<BbqCategoryId, Long> {

    @Override
    public Long convertToDatabaseColumn(BbqCategoryId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public BbqCategoryId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : BbqCategoryId.of(dbData);
    }
}
