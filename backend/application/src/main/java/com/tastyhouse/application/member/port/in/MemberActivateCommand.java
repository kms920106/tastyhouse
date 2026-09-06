package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
