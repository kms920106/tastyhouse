package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.review.vo.ReviewId;

@Converter
public class ReviewIdConverter implements AttributeConverter<ReviewId, Long> {

    @Override
    public Long convertToDatabaseColumn(ReviewId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ReviewId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ReviewId.of(dbData);
    }
}
