package com.tastyhouse.application.member.port.out;

import java.time.LocalDateTime;

public record MemberListItemResult(
    Long id,
    String username,
    String nickname,
    String fullName,
    String phoneNumber,
    String gender,
    String memberGrade,
    String memberStatus,
    String profileImageUrl,
    LocalDateTime createdAt
) {
}
