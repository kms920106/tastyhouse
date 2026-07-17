package com.tastyhouse.core.domain.member.application.dto;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;

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
