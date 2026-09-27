package com.tastyhouse.application.order.port.out.write;

import java.math.BigDecimal;

public record OrderDeliveryDestinationSnapshot(
    Long adminDongId,
    String detailAddress,
    Integer distanceMeters,
    BigDecimal latitude,
    BigDecimal longitude,
    String lotAddress,
    String roadAddress
) {
}
