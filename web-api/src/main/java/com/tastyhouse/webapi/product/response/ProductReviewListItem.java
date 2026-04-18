package com.tastyhouse.webapi.product.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProductReviewListItem(
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
    public static ProductReviewListItem from(
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
    return new ProductReviewListItem(
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
