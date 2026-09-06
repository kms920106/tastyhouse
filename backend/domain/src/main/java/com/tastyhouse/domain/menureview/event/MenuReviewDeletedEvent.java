package com.tastyhouse.domain.menureview.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public record MenuReviewDeletedEvent(
    MenuReviewId menuReviewId,
    MemberId memberId,
    ShopId shopId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
