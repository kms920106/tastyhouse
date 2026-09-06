package com.tastyhouse.application.member.port.out;

import com.tastyhouse.domain.member.model.MemberGrade;

public record MemberWithProfileImageResult(
    Long id,
    String nickname,
    MemberGrade memberGrade,
    String statusMessage,
    String profileImageUrl
) {
}
