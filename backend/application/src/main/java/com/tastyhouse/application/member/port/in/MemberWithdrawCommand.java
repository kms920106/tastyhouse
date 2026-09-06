package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberWithdrawCommand(
    Long memberId,
    String reason,
    String reasonDetail
) {
    public MemberWithdrawCommand {
        if (memberId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
