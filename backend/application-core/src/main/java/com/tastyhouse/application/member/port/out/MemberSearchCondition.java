package com.tastyhouse.application.member.port.out;

public record MemberSearchCondition(
    String nickname,
    String username,
    String phone,
    String status,
    String grade
) {

    public static MemberSearchCondition of(
        String nickname,
        String username,
        String phone,
        String status,
        String grade
    ) {
        return new MemberSearchCondition(nickname, username, phone, status, grade);
    }
}
