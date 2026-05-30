package com.tastyhouse.core.domain.order.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;

import java.time.LocalDateTime;

public record OrderListItemResult(
    Long id,
    String shopName,
    String shopThumbnailImageFilePath,
    String firstProductName,
    Integer totalItemCount,
    Integer amount,
    PaymentStatus paymentStatus,
    LocalDateTime paymentDate
) {
    @QueryProjection
    public OrderListItemResult {
    }
}
