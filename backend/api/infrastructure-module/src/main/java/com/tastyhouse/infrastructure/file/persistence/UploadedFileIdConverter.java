package com.tastyhouse.infrastructure.file.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.file.vo.UploadedFileId;

@Converter
public class UploadedFileIdConverter implements AttributeConverter<UploadedFileId, Long> {

    @Override
    public Long convertToDatabaseColumn(UploadedFileId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public UploadedFileId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : UploadedFileId.of(dbData);
    }
}
