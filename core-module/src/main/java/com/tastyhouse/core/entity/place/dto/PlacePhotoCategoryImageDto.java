package com.tastyhouse.core.entity.place.dto;

public record PlacePhotoCategoryImageDto(
    Long id,
    Long placePhotoCategoryId,
    String filePath,
    Integer sort
) {
}
