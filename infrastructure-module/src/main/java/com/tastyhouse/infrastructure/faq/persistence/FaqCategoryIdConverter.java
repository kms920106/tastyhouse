package com.tastyhouse.infrastructure.faq.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.faq.domain.vo.FaqCategoryId;

@Converter
public class FaqCategoryIdConverter implements AttributeConverter<FaqCategoryId, Long> {

    @Override
    public Long convertToDatabaseColumn(FaqCategoryId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public FaqCategoryId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : FaqCategoryId.of(dbData);
    }
}
