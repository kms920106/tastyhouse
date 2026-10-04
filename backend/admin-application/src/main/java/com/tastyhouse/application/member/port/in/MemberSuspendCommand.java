package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MemberSuspendCommand(Long memberId) {

    public MemberSuspendCommand {
        if (memberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static MemberSuspendCommand of(Long memberId) {
        return new MemberSuspendCommand(memberId);
    }
}
