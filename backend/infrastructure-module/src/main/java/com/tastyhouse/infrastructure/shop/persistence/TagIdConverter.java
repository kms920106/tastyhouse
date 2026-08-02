package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.shop.vo.TagId;

@Converter
public class TagIdConverter implements AttributeConverter<TagId, Long> {

    @Override
    public Long convertToDatabaseColumn(TagId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public TagId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : TagId.of(dbData);
    }
}
