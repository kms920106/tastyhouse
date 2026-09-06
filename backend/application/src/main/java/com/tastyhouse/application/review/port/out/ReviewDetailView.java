package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDetailView(
    Long id,
    Long shopId,
    String shopName,
    String stationName,
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
    List<String> tagNames,
    boolean ownerOnly,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt,
    String orderMethod,
    Integer deliveryRating,
    String deliveryComment
) {
}
