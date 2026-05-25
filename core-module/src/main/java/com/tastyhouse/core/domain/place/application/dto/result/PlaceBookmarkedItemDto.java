package com.tastyhouse.core.domain.place.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record PlaceBookmarkedItemDto(
    Long placeId,
    Long bookmarkId,
    String placeName,
    String stationName,
    Double rating,
    String imageUrl,
    boolean bookmarked
) {
    @QueryProjection
    public PlaceBookmarkedItemDto {
    }
}
