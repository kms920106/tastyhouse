package com.tastyhouse.application.member.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberDeliveryAddressUpdateCommand(
    Long memberId,
    Long addressId,
    String alias,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public MemberDeliveryAddressUpdateCommand {
        if (memberId == null || addressId == null || roadAddress == null
            || latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
