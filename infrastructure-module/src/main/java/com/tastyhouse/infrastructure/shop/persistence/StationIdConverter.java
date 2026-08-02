package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.shop.domain.vo.StationId;

@Converter
public class StationIdConverter implements AttributeConverter<StationId, Long> {

    @Override
    public Long convertToDatabaseColumn(StationId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public StationId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : StationId.of(dbData);
    }
}
