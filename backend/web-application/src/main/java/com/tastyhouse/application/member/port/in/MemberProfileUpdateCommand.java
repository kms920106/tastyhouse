package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MemberProfileUpdateCommand(
    Long memberId,
    String nickname,
    String statusMessage,
    Long profileImageFileId
) {

    public MemberProfileUpdateCommand {
        if (memberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
