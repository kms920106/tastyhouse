package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewProductView(
    Long productId,
    String productName,
    String productImageUrl,
    Integer productPrice,
    Long reviewId,
    String content,
    Double totalRating,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating,
    boolean willRevisit,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    List<String> imageUrls,
    List<String> tagNames
) {
}
