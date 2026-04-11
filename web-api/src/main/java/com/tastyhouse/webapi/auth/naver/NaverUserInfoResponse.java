package com.tastyhouse.webapi.auth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        @JsonProperty("mobile") String mobile
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
}
