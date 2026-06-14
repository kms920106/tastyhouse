package com.tastyhouse.webapi.product.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProductReviewListItemResponse(
    Long id,
    List<String> imageUrls,
    Double totalRating,
    String content,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    Long productId,
    String productName
) {
    public static ProductReviewListItemResponse from(
    Long id,
    List<String> imageUrls,
    Double totalRating,
    String content,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    Long productId,
    String productName
    ) {
    return new ProductReviewListItemResponse(
        id,
        imageUrls,
        totalRating,
        content,
        memberId,
        memberNickname,
        memberProfileImageUrl,
        createdAt,
        productId,
        productName
    );
    }
}
