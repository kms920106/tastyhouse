package com.tastyhouse.core.entity.place.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;

import java.util.List;

public record EditorChoiceDto(
    Long id,
    Long placeId,
    String name,
    String title,
    String content,
    String placeImageUrl,
    List<ProductSimpleResult> products
) {
    @QueryProjection
    public EditorChoiceDto {
    }
}
