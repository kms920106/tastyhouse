package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.entity.payment.PaymentStatus;
import com.tastyhouse.core.entity.payment.dto.OrderListItemDto;

import java.time.LocalDateTime;

public record OrderListItemResponse(
    Long id,
    String placeName,
    String placeThumbnailImageUrl,
    String firstProductName,
    Integer totalItemCount,
    Integer amount,
    PaymentStatus paymentStatus,
    LocalDateTime paymentDate
) {
    public static OrderListItemResponse from(OrderListItemDto dto, String placeThumbnailImageUrl) {
        return new OrderListItemResponse(
            dto.id(),
            dto.placeName(),
            placeThumbnailImageUrl,
            dto.firstProductName(),
            dto.totalItemCount(),
            dto.amount(),
            dto.paymentStatus(),
            dto.paymentDate()
        );
    }
}
