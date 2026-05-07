package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.entity.user.MemberGrade;

public record MemberMeResponse(
    Long id,
    String nickname,
    MemberGrade grade,
    String statusMessage,
    String profileImageUrl,
    String fullName,
    String phoneNumber,
    String email
) {
    public static MemberMeResponse from(
    Long id,
    String nickname,
    MemberGrade grade,
    String statusMessage,
    String profileImageUrl,
    String fullName,
    String phoneNumber,
    String email
    ) {
    return new MemberMeResponse(
        id,
        nickname,
        grade,
        statusMessage,
        profileImageUrl,
        fullName,
        phoneNumber,
        email
    );
    }
}
