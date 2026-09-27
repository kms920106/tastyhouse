package com.tastyhouse.application.member.port.out.write;

import java.time.LocalDateTime;

public record MemberSocialAccountState(
    Long id,
    Long memberId,
    String provider,
    String providerId,
    String providerEmail,
    String providerNickname,
    String providerProfileImageUrl,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
