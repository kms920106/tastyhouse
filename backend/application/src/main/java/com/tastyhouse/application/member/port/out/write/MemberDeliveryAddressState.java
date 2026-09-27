package com.tastyhouse.application.member.port.out.write;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberDeliveryAddressState(
    Long id,
    Long memberId,
    String alias,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    Long adminDongId,
    BigDecimal latitude,
    BigDecimal longitude,
    boolean defaultAddress,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
