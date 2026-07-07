package com.tastyhouse.core.domain.member.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record ProfileUpdateCommand(
    MemberId memberId,
    String nickname,
    String statusMessage,
    Long profileImageFileId
) {

    public static ProfileUpdateCommand of(
        MemberId memberId,
        String nickname,
        String statusMessage,
        Long profileImageFileId
    ) {
        return new ProfileUpdateCommand(memberId, nickname, statusMessage, profileImageFileId);
    }
}
