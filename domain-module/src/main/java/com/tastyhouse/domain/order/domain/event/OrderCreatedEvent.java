package com.tastyhouse.domain.order.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

public record OrderCreatedEvent(
    OrderId orderId,
    MemberId memberId,
    ShopId shopId,
    Integer finalAmount,
    LocalDateTime createdAt
) {
}
