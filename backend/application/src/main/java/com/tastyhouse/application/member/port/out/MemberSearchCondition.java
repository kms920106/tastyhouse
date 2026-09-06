package com.tastyhouse.application.member.port.out;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;

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
