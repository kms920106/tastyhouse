package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;

public record ReviewState(
    Long id,
    Long shopId,
    Long productId,
    Long memberId,
    String content,
    Double totalRating,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating,
    boolean willRevisit,
    Long orderId,
    boolean hidden,
    boolean ownerOnly,
    Integer deliveryRating,
    String deliveryComment,
    LocalDateTime createdAt
) {
}
