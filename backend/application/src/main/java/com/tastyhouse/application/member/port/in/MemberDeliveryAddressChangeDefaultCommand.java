package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 기본 배달 주소 지정 command. 요청 본문이 없는 상태전이라 컨트롤러가 정적 팩토리로 조립한다.
 */
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
