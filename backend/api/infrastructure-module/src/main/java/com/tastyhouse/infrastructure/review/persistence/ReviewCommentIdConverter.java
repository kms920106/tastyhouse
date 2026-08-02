package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.review.vo.ReviewCommentId;

@Converter
public class ReviewCommentIdConverter implements AttributeConverter<ReviewCommentId, Long> {

    @Override
    public Long convertToDatabaseColumn(ReviewCommentId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ReviewCommentId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ReviewCommentId.of(dbData);
    }
}
