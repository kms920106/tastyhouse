package com.tastyhouse.external.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tastyhouse.core.entity.user.Gender;

// 네이버 프로필 API 응답 (GET https://openapi.naver.com/v1/nid/me)
// 사용자 정보는 최상위 "response" 객체 안에 중첩된다.
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

    // 네이버 gender: "M" → MALE, "F" → FEMALE, 그 외 → null
    public Gender getGender() {
        if (response == null) return null;
        return switch (response.gender()) {
            case "M" -> Gender.MALE;
            case "F" -> Gender.FEMALE;
            default -> null;
        };
    }

    public String getBirthYear() {
        if (response == null) return null;
        return response.birthYear();
    }

    // 네이버 birthday: "MM-DD" 형식 → 월 부분만 반환 (선행 0 제거: "01" → "1")
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

    // 네이버 birthday: "MM-DD" 형식 → 일 부분만 반환 (선행 0 제거: "05" → "5")
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
