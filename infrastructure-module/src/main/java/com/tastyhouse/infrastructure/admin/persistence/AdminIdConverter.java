package com.tastyhouse.infrastructure.admin.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.admin.domain.vo.AdminId;

@Converter
public class AdminIdConverter implements AttributeConverter<AdminId, Long> {

    @Override
    public Long convertToDatabaseColumn(AdminId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public AdminId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : AdminId.of(dbData);
    }
}
