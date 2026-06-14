package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;

import java.time.LocalDateTime;

public record OrderListItemResponse(
    Long id,
    String shopName,
    String shopThumbnailImageUrl,
    String firstProductName,
    Integer totalItemCount,
    Integer amount,
    PaymentStatus paymentStatus,
    LocalDateTime paymentDate
) {
    public static OrderListItemResponse from(
        Long id,
        String shopName,
        String shopThumbnailImageUrl,
        String firstProductName,
        Integer totalItemCount,
        Integer amount,
        PaymentStatus paymentStatus,
        LocalDateTime paymentDate
    ) {
        return new OrderListItemResponse(
            id,
            shopName,
            shopThumbnailImageUrl,
            firstProductName,
            totalItemCount,
            amount,
            paymentStatus,
            paymentDate
        );
    }
}
