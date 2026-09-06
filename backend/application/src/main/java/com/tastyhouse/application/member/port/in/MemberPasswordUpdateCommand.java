package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberPasswordUpdateCommand(
    Long memberId,
    String newPassword,
    String newPasswordConfirm
) {
    public MemberPasswordUpdateCommand {
        if (memberId == null || newPassword == null || newPasswordConfirm == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
