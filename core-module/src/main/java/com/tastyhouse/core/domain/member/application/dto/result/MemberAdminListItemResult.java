package com.tastyhouse.core.domain.member.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.model.MemberGender;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;

public record MemberAdminListItemResult(
    Long id,
    String username,
    String nickname,
    String fullName,
    String phoneNumber,
    MemberGender gender,
    MemberGrade memberGrade,
    MemberStatus memberStatus,
    String profileImageFilePath,
    LocalDateTime createdAt
) {
}
