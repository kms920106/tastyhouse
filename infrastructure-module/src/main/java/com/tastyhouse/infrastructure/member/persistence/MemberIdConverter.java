package com.tastyhouse.infrastructure.member.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

@Converter
public class MemberIdConverter implements AttributeConverter<MemberId, Long> {

    @Override
    public Long convertToDatabaseColumn(MemberId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public MemberId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : MemberId.of(dbData);
    }
}
