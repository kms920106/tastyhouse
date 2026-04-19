package com.tastyhouse.core.entity.user.dto;

import com.tastyhouse.core.entity.user.MemberGrade;

public record MemberProfileDetailDto(
    Long id,
    String nickname,
    MemberGrade memberGrade,
    String statusMessage,
    String profileImageFilePath,
    String fullName,
    String phoneNumber,
    String username
) {
}
