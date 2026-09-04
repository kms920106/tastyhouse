package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달 주소 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다.
 */
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
