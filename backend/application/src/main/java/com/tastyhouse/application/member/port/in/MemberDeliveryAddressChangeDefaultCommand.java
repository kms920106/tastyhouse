package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberDeliveryAddressChangeDefaultCommand(
    Long memberId,
    Long addressId
) {
    public MemberDeliveryAddressChangeDefaultCommand {
        if (memberId == null || addressId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static MemberDeliveryAddressChangeDefaultCommand of(Long memberId, Long addressId) {
        return new MemberDeliveryAddressChangeDefaultCommand(memberId, addressId);
    }
}
