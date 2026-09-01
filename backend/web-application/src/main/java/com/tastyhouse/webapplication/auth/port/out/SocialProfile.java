package com.tastyhouse.webapplication.auth.port.out;

/**
 * 제공자 중립 소셜 프로필.
 *
 * <p>제공자마다 제공 가능한 항목이 달라 미제공 항목은 {@code null}이다.
 * <ul>
 *   <li>카카오: providerId, email, nickname, profileImageUrl, name(동의 시), phoneNumber(동의 시), gender(동의 시)</li>
 *   <li>네이버: 전 항목</li>
 *   <li>페이스북: providerId, email, name, profileImageUrl</li>
 *   <li>애플: providerId, email (이름은 최초 동의 시 form_post로만 전달되어 id_token에 없음)</li>
 * </ul>
 *
 * <p><b>전 필드가 {@code String}이다.</b> {@code gender}도 도메인 enum이 아니라 그 상수명 문자열
 * ({@code "MALE"}/{@code "FEMALE"}/{@code null})을 담는다 — 외부 연동 타입이 도메인 타입을 보유하면
 * external-api → domain-module 역방향 결합이 생기기 때문이다. 도메인 enum 승격이 필요하면 소비 측
 * (web-api Service)이 {@code MemberGender.from(String)}으로 수행한다(도메인 enum 경계 규칙).
 */
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
