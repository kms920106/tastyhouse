package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.product.domain.vo.BbqMenuId;

@Converter
public class BbqMenuIdConverter implements AttributeConverter<BbqMenuId, Long> {

    @Override
    public Long convertToDatabaseColumn(BbqMenuId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public BbqMenuId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : BbqMenuId.of(dbData);
    }
}
