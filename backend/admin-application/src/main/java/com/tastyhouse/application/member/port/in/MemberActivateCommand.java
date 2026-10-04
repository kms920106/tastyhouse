package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MemberActivateCommand(Long memberId) {

    public MemberActivateCommand {
        if (memberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static MemberActivateCommand of(Long memberId) {
        return new MemberActivateCommand(memberId);
    }
}
