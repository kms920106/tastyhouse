package com.tastyhouse.domain.order.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;

public record OrderCreatedEvent(
    OrderId orderId,
    MemberId memberId,
    Long shopId,
    Integer finalAmount,
    LocalDateTime createdAt
) {
}
