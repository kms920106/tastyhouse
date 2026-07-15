package com.tastyhouse.core.domain.order.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;

public record OrderCreatedEvent(
    OrderId orderId,
    MemberId memberId,
    Long shopId,
    Integer finalAmount,
    LocalDateTime createdAt
) {
}
