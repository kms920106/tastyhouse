package com.tastyhouse.domain.order.vo;

import java.math.BigDecimal;

public record OrderDeliveryDestination(
    Long adminDongId,
    String detailAddress,
    Integer distanceMeters,
    BigDecimal latitude,
    BigDecimal longitude,
    String lotAddress,
    String roadAddress
) {
    public static OrderDeliveryDestination of(
        Long adminDongId,
        String detailAddress,
        int distanceMeters,
        BigDecimal latitude,
        BigDecimal longitude,
        String lotAddress,
        String roadAddress
    ) {
        return new OrderDeliveryDestination(
            adminDongId,
            detailAddress,
            distanceMeters,
            latitude,
            longitude,
            lotAddress,
            roadAddress
        );
    }

    public static OrderDeliveryDestination none() {
        return new OrderDeliveryDestination(null, null, null, null, null, null, null);
    }

    public boolean isPresent() {
        return this.roadAddress != null;
    }
}
