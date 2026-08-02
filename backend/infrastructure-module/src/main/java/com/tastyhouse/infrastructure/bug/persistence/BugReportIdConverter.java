package com.tastyhouse.infrastructure.bug.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.bug.vo.BugReportId;

@Converter
public class BugReportIdConverter implements AttributeConverter<BugReportId, Long> {

    @Override
    public Long convertToDatabaseColumn(BugReportId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public BugReportId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : BugReportId.of(dbData);
    }
}
