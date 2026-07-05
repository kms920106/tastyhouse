package com.tastyhouse.core.domain.member.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record UpdateProfileCommand(
    MemberId memberId,
    String nickname,
    String statusMessage,
    Long profileImageFileId
) {

    public static UpdateProfileCommand of(
        MemberId memberId,
        String nickname,
        String statusMessage,
        Long profileImageFileId
    ) {
        return new UpdateProfileCommand(memberId, nickname, statusMessage, profileImageFileId);
    }
}
