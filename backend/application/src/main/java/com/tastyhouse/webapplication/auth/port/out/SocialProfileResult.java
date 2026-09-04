package com.tastyhouse.webapplication.auth.port.out;

/**
 * 소셜 로그인 계정 연동 시 회원가입 폼 자동 매핑용 공통 프로필.
 *
 * <p>소셜 플랫폼마다 제공 가능한 필드가 다르므로 미제공 항목은 null이다. 플랫폼별 제공 필드:
 * <ul>
 *   <li>카카오: providerId, email, nickname, profileImageUrl, name(동의 시), phoneNumber(동의 시), gender(동의 시)</li>
 *   <li>네이버: providerId, email, nickname, profileImageUrl, name, phoneNumber, gender, birthYear, birthMonth, birthDay</li>
 *   <li>페이스북: providerId, email, name, profileImageUrl</li>
 *   <li>애플: providerId, email</li>
 * </ul>
 *
 * <p><b>챕터 10</b>에서 신설. 이 값은 DB 조회 결과가 아니라 external-api의 소셜 SPI
 * ({@code SocialProfile})에서 옮겨 담은 것이라 공유 읽기 계약 패키지에 형제가 없다. 앱 네임스페이스에
 * 두는 근거는 {@link MemberJwtResult}와 같다.
 *
 * <p><b>null 필드 생략은 계약이다</b> — 대응 표현 계약 {@code AuthSocialProfileResponse}가
 * {@code @JsonInclude(NON_NULL)}을 갖는다. 그 어노테이션은 직렬화가 일어나는 web-api의 Response에
 * 있어야 하며 이 record로 옮기면 무의미해진다(챕터 10 스펙 §b).
 */
public record SocialProfileResult(
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
