package com.tastyhouse.core.domain.place.application.dto.result;

public record PlacePhotoCategoryImageDto(
    Long id,
    Long placePhotoCategoryId,
    String filePath,
    Integer sort
) {
}
