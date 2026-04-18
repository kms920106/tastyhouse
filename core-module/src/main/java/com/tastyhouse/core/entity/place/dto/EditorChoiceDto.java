package com.tastyhouse.core.entity.place.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.entity.product.dto.ProductSimpleDto;

import java.util.List;

public record EditorChoiceDto(
    Long id,
    Long placeId,
    String name,
    String title,
    String content,
    String placeImageUrl,
    List<ProductSimpleDto> products
) {
    @QueryProjection
    public EditorChoiceDto {
    }
}
