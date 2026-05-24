package com.tastyhouse.core.domain.member.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record UpdateProfileCommand(
    MemberId memberId,
    String nickname,
    String statusMessage,
    Long profileImageFileId
) {
}
