package com.tastyhouse.webapi.shop.response;

import java.time.LocalDateTime;
import java.util.List;

public record ShopReviewListItemResponse(
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
    public static ShopReviewListItemResponse from(
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
        return new ShopReviewListItemResponse(
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
