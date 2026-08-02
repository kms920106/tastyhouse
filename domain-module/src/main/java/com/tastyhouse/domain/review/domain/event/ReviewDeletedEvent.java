package com.tastyhouse.domain.review.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.product.domain.vo.ProductId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

public record ReviewDeletedEvent(
    ReviewId reviewId,
    MemberId memberId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
