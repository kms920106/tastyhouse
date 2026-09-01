package com.tastyhouse.application.member.port.out;

/**
 * 회원 개인정보 조회 결과.
 *
 * <p>마이페이지 개인정보 화면이 그대로 표시하는 필드 묶음이다. 과거에는 이 조회가 write 포트
 * ({@code MemberRepository#findById})로 회원 애그리거트를 통째로 로드해 필드를 꺼내 쓰는 형태였고,
 * 그 탓에 {@code MemberQueryService}가 write 포트를 들고 있어야 해 CQRS 교차 주입 금지 규칙의 예외로
 * 남아 있었다. 표현 목적 조회이므로 읽기 포트의 투영으로 내린다.
 *
 * <p>{@code gender}는 도메인 enum이 아니라 {@code String}으로 담는다 — 이 값은 HTTP 경계까지 그대로
 * 전달되는 표현용이고, query 계층 규약대로 도메인 타입을 응답 경로에 싣지 않는다.
 */
public record MemberPersonalInfoResult(
    String username,
    String fullName,
    String phoneNumber,
    Integer birthDate,
    String gender,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled
) {
}
