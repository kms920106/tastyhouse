package com.tastyhouse.core.entity.place.dto;

import com.querydsl.core.annotations.QueryProjection;

public record PlaceBookmarkedItemDto(
    Long placeId,
    Long bookmarkId,
    String placeName,
    String stationName,
    Double rating,
    String imageUrl,
    Boolean isBookmarked
) {
    @QueryProjection
    public PlaceBookmarkedItemDto {
    }
}
