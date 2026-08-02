package com.tastyhouse.infrastructure.ceo.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.ceo.domain.vo.CeoId;

@Converter
public class CeoIdConverter implements AttributeConverter<CeoId, Long> {

    @Override
    public Long convertToDatabaseColumn(CeoId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CeoId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : CeoId.of(dbData);
    }
}
