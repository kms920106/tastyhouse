package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MemberManagementWithdrawCommand(
    Long memberId,
    String reason,
    String reasonDetail
) {

    public MemberManagementWithdrawCommand {
        if (memberId == null || reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
