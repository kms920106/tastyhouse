package com.tastyhouse.domain.review.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.product.domain.vo.ProductId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

public record ReviewCreatedEvent(
    ReviewId reviewId,
    MemberId memberId,
    ShopId shopId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
