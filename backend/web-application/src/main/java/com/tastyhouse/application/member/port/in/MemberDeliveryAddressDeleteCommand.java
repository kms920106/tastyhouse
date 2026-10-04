package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MemberDeliveryAddressDeleteCommand(
    Long memberId,
    Long addressId
) {

    public MemberDeliveryAddressDeleteCommand {
        if (memberId == null || addressId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static MemberDeliveryAddressDeleteCommand of(Long memberId, Long addressId) {
        return new MemberDeliveryAddressDeleteCommand(memberId, addressId);
    }
}
