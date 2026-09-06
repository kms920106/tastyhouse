package com.tastyhouse.application.member.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberDeliveryAddressCreateCommand(
    Long memberId,
    String alias,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean isDefault
) {
    public MemberDeliveryAddressCreateCommand {
        if (memberId == null || roadAddress == null || latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
