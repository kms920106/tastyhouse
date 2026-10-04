package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MemberDeliveryAddressChangeDefaultCommand(
    Long memberId,
    Long addressId
) {

    public MemberDeliveryAddressChangeDefaultCommand {
        if (memberId == null || addressId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static MemberDeliveryAddressChangeDefaultCommand of(Long memberId, Long addressId) {
        return new MemberDeliveryAddressChangeDefaultCommand(memberId, addressId);
    }
}
