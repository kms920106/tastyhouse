package com.tastyhouse.application.member.port.out;

import java.math.BigDecimal;

public record MemberDeliveryAddressItemResult(
    Long id,
    String alias,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    Long adminDongId,
    String regionName,
    BigDecimal latitude,
    BigDecimal longitude,
    boolean defaultAddress
) {
}
