package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberProfileUpdateCommand(
    Long memberId,
    String nickname,
    String statusMessage,
    Long profileImageFileId
) {
    public MemberProfileUpdateCommand {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
