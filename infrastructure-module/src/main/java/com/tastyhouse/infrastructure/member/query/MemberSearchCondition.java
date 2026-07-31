package com.tastyhouse.infrastructure.member.query;

import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;

/**
 * 회원 관리 목록 검색 조건.
 *
 * <p>HTTP 경계에서 받은 {@code String} enum 후보값은 소비 모듈의 QueryService가 core enum으로 승격한
 * 뒤 이 조건으로 조립한다(도메인 enum 경계 규칙).
 */
public record MemberSearchCondition(
    String nickname,
    String username,
    String phone,
    MemberStatus status,
    MemberGrade grade
) {

    public static MemberSearchCondition of(
        String nickname,
        String username,
        String phone,
        MemberStatus status,
        MemberGrade grade
    ) {
        return new MemberSearchCondition(nickname, username, phone, status, grade);
    }
}
