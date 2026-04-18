package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.entity.place.dto.MyBookmarkedPlaceItemDto;

public record MyBookmarkedPlaceListItemResponse(
    Long placeId,
    Long bookmarkId,
    String placeName,
    String stationName,
    Double rating,
    String imageUrl,
    Boolean isBookmarked
) {
    public static MyBookmarkedPlaceListItemResponse from(MyBookmarkedPlaceItemDto dto) {
    return new MyBookmarkedPlaceListItemResponse(
        dto.placeId(),
        dto.bookmarkId(),
        dto.placeName(),
        dto.stationName(),
        dto.rating(),
        dto.imageUrl(),
        dto.isBookmarked()
    );
    }
}
