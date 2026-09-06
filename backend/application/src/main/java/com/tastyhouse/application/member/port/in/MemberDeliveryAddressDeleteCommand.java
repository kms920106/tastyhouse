package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberDeliveryAddressDeleteCommand(
    Long memberId,
    Long addressId
) {
    public MemberDeliveryAddressDeleteCommand {
        if (memberId == null || addressId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static MemberDeliveryAddressDeleteCommand of(Long memberId, Long addressId) {
        return new MemberDeliveryAddressDeleteCommand(memberId, addressId);
    }
}
