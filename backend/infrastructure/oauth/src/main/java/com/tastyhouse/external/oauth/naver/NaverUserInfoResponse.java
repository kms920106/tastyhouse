package com.tastyhouse.external.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserInfoResponse(
    @JsonProperty("resultcode") String resultCode,
    @JsonProperty("message") String message,
    @JsonProperty("response") NaverProfile response
) {
    public record NaverProfile(
    @JsonProperty("id") String id,
    @JsonProperty("email") String email,
    @JsonProperty("name") String name,
    @JsonProperty("nickname") String nickname,
    @JsonProperty("profile_image") String profileImage,
    @JsonProperty("gender") String gender,
    @JsonProperty("birthday") String birthday,
    @JsonProperty("birthyear") String birthYear,
    @JsonProperty("mobile") String mobile,
    @JsonProperty("age") String age
    ) {}

    public String getProviderId() {
    if (response == null) return null;
    return response.id();
    }

    public String getEmail() {
    if (response == null) return null;
    return response.email();
    }

    public String getName() {
    if (response == null) return null;
    return response.name();
    }

    public String getNickname() {
    if (response == null) return null;
    return response.nickname();
    }

    public String getProfileImageUrl() {
    if (response == null) return null;
    return response.profileImage();
    }

    public String getMobile() {
    if (response == null) return null;
    return response.mobile();
    }

    public String getGender() {
    if (response == null || response.gender() == null) return null;
    return switch (response.gender()) {
        case "M" -> "MALE";
        case "F" -> "FEMALE";
        default -> null;
    };
    }

    public String getBirthYear() {
    if (response == null) return null;
    return response.birthYear();
    }

    public String getBirthMonth() {
    if (response == null || response.birthday() == null) return null;
    String[] parts = response.birthday().split("-");
    if (parts.length < 2) return null;
    try {
        return String.valueOf(Integer.parseInt(parts[0]));
    } catch (NumberFormatException e) {
        return null;
    }
    }

    public String getBirthDay() {
    if (response == null || response.birthday() == null) return null;
    String[] parts = response.birthday().split("-");
    if (parts.length < 2) return null;
    try {
        return String.valueOf(Integer.parseInt(parts[1]));
    } catch (NumberFormatException e) {
        return null;
    }
    }
}
