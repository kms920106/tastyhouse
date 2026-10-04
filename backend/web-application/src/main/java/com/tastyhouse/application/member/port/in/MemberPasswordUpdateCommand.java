package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MemberPasswordUpdateCommand(
    Long memberId,
    String newPassword,
    String newPasswordConfirm
) {

    public MemberPasswordUpdateCommand {
        if (memberId == null || newPassword == null || newPasswordConfirm == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
