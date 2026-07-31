package com.tastyhouse.infrastructure.member.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.domain.model.MemberGender;
import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;

/**
 * 회원 관리 목록 read model(admin-api 소비).
 *
 * <p>비-admin 형제 Result가 같은 패키지에 없으므로 {@code Management} 한정어 없이 순수명을 쓴다
 * (admin 전용 네이밍 규칙).
 */
public record MemberListItemResult(
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

    @QueryProjection
    public MemberListItemResult {
    }
}
