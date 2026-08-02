package com.tastyhouse.domain.review.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.vo.ReviewId;

public record ReviewDeletedEvent(
    ReviewId reviewId,
    MemberId memberId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
