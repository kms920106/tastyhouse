package com.tastyhouse.infrastructure.rank.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.rank.domain.vo.RankPeriodId;

@Converter
public class RankPeriodIdConverter implements AttributeConverter<RankPeriodId, Long> {

    @Override
    public Long convertToDatabaseColumn(RankPeriodId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public RankPeriodId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : RankPeriodId.of(dbData);
    }
}
