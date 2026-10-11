package com.tastyhouse.application.member.port.out;

public record MemberManagementDetailWithProfileImageResult(
    MemberManagementDetailResult member,
    String profileImageUrl
) {
}
