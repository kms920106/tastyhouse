package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberSuspendCommand(Long memberId) {
    public MemberSuspendCommand {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static MemberSuspendCommand of(Long memberId) {
        return new MemberSuspendCommand(memberId);
    }
}
