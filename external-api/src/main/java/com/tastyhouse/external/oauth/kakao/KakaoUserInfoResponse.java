package com.tastyhouse.external.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.tastyhouse.core.domain.member.domain.model.MemberGender;

public record KakaoUserInfoResponse(
    @JsonProperty("id") Long id,
    @JsonProperty("connected_at") String connectedAt,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
    @JsonProperty("profile") Profile profile,
    @JsonProperty("email_needs_agreement") Boolean emailNeedsAgreement,
    @JsonProperty("is_email_valid") Boolean isEmailValid,
    @JsonProperty("is_email_verified") Boolean isEmailVerified,
    @JsonProperty("email") String email,
    @JsonProperty("name_needs_agreement") Boolean nameNeedsAgreement,
    @JsonProperty("name") String name,
    @JsonProperty("gender_needs_agreement") Boolean genderNeedsAgreement,
    @JsonProperty("gender") String gender,
    @JsonProperty("phone_number_needs_agreement") Boolean phoneNumberNeedsAgreement,
    @JsonProperty("phone_number") String phoneNumber
    ) {}

    public record Profile(
    @JsonProperty("nickname") String nickname,
    @JsonProperty("thumbnail_image_url") String thumbnailImageUrl,
    @JsonProperty("profile_image_url") String profileImageUrl,
    @JsonProperty("is_default_image") Boolean isDefaultImage
    ) {}

    public String getEmail() {
    if (kakaoAccount == null) return null;
    return kakaoAccount.email();
    }

    public String getNickname() {
    if (kakaoAccount == null || kakaoAccount.profile() == null) return null;
    return kakaoAccount.profile().nickname();
    }

    public String getProfileImageUrl() {
    if (kakaoAccount == null || kakaoAccount.profile() == null) return null;
    return kakaoAccount.profile().profileImageUrl();
    }

    public String getName() {
    if (kakaoAccount == null) return null;
    return kakaoAccount.name();
    }

    public String getPhoneNumber() {
    if (kakaoAccount == null) return null;
    return kakaoAccount.phoneNumber();
    }

    // 카카오 gender: "male" → MALE, "female" → FEMALE, 그 외 → null
    public MemberGender getGender() {
    if (kakaoAccount == null || kakaoAccount.gender() == null) return null;
    return switch (kakaoAccount.gender()) {
        case "male" -> MemberGender.MALE;
        case "female" -> MemberGender.FEMALE;
        default -> null;
    };
    }
}
