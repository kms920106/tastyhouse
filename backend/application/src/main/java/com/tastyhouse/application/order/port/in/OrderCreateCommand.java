package com.tastyhouse.application.order.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record OrderCreateCommand(
    Long memberId,
    Long shopId,
    String orderMethod,
    List<OrderLineCommand> orderLines,
    Long memberCouponId,
    Integer usePoint,
    Long deliveryAddressId,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer deliveryTipAmount,
    Integer cupDepositAmount,
    Integer finalAmount,
    LocalDateTime scheduledAt
) {
    public OrderCreateCommand {
        if (memberId == null || shopId == null || orderMethod == null || orderLines == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
