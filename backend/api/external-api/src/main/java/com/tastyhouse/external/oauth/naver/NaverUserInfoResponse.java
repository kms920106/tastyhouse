package com.tastyhouse.external.oauth.naver;

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

    // 네이버 gender: "M" → "MALE", "F" → "FEMALE", 그 외 → null
    // 도메인 enum(MemberGender)이 아니라 그 상수명 문자열을 반환한다 — 외부 응답 DTO가 도메인 타입을
    // 보유하면 external-api → domain-module 역방향 결합이 생기기 때문이다. 도메인 enum 승격이 필요하면
    // 소비 측(web-api Service)이 MemberGender.from(String)으로 수행한다(도메인 enum 경계 규칙).
    // response.gender()는 사용자가 성별 제공에 동의하지 않으면 null이므로 반드시 가드한다(카카오 형제와 동일).
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
