package com.tastyhouse.core.entity.payment.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.entity.payment.PaymentStatus;

import java.time.LocalDateTime;

public record OrderListItemDto(
    Long id,
    String placeName,
    String placeThumbnailImageUrl,
    String firstProductName,
    Integer totalItemCount,
    Integer amount,
    PaymentStatus paymentStatus,
    LocalDateTime paymentDate
) {
    @QueryProjection
    public OrderListItemDto {
    }
}
