package com.tastyhouse.application.auth.port.out;

public record SocialProfile(
    String providerId,
    String email,
    String nickname,
    String profileImageUrl,
    String name,
    String phoneNumber,
    String gender,
    String birthYear,
    String birthMonth,
    String birthDay
) {
}
