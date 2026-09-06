package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberManagementWithdrawCommand(
    Long memberId,
    String reason,
    String reasonDetail
) {
    public MemberManagementWithdrawCommand {
        if (memberId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
