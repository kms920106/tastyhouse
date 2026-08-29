package com.tastyhouse.adminapi.member.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 정지 해제 command. 요청 본문이 없는 상태 전이이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record MemberActivateCommand(Long memberId) {
    public MemberActivateCommand {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static MemberActivateCommand of(Long memberId) {
        return new MemberActivateCommand(memberId);
    }
}
