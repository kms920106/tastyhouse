package com.tastyhouse.infrastructure.event.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.event.domain.vo.EventId;

@Converter
public class EventIdConverter implements AttributeConverter<EventId, Long> {

    @Override
    public Long convertToDatabaseColumn(EventId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public EventId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : EventId.of(dbData);
    }
}
