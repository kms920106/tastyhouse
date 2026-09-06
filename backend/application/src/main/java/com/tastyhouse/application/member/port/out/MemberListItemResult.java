package com.tastyhouse.application.member.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;

public record MemberListItemResult(
    Long id,
    String username,
    String nickname,
    String fullName,
    String phoneNumber,
    MemberGender gender,
    MemberGrade memberGrade,
    MemberStatus memberStatus,
    String profileImageUrl,
    LocalDateTime createdAt
) {
}
