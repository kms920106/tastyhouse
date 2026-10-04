package com.tastyhouse.infrastructure.persistence.order.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;

@Embeddable
public record OrderDeliveryDestinationEmbeddable(
    Long adminDongId,
    String detailAddress,
    Integer distanceMeters,
    BigDecimal latitude,
    BigDecimal longitude,
    String lotAddress,
    String roadAddress
) {
}
