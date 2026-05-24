package com.tastyhouse.core.domain.member.application.dto.result;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;

public record MemberWithProfileImageResult(
    Long id,
    String nickname,
    MemberGrade memberGrade,
    String statusMessage,
    String profileImageFilePath
) {
}
