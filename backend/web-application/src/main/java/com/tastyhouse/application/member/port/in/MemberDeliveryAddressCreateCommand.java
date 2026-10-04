package com.tastyhouse.application.member.port.in;

import java.math.BigDecimal;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
