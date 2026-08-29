package com.tastyhouse.webapi.member.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 프로필 변경 command.
 *
 * <p>{@code statusMessage}·{@code profileImageFileId}는 비우는 것이 정상 흐름이라 null을 허용한다.
 */
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
