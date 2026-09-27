package com.tastyhouse.application.member.port.out;

public record MemberWithProfileImageResult(
    Long id,
    String nickname,
    String memberGrade,
    String statusMessage,
    String profileImageUrl
) {
}
