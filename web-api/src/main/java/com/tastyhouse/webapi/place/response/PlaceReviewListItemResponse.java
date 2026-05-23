package com.tastyhouse.webapi.place.response;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceReviewListItemResponse(
    Long id,
    List<String> imageUrls,
    Double totalRating,
    String content,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    Long productId,
    String productName
) {
    public static PlaceReviewListItemResponse from(
        Long id,
        List<String> imageUrls,
        Double totalRating,
        String content,
        String memberNickname,
        String memberProfileImageUrl,
        LocalDateTime createdAt,
        Long productId,
        String productName
    ) {
        return new PlaceReviewListItemResponse(
            id,
            imageUrls,
            totalRating,
            content,
            memberNickname,
            memberProfileImageUrl,
            createdAt,
            productId,
            productName
        );
    }
}
