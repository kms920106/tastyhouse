package com.tastyhouse.application.member.port.out;

import java.time.LocalDateTime;

/**
 * 회원 관리 상세 조회 결과.
 *
 * <p>web 개인정보 형제인 {@code MemberPersonalInfoResult}와 달리 관리 화면이 필요로 하는 식별자·등급·
 * 상태·가입일까지 담는다. 두 화면의 필드 셋이 달라 통합하지 않는다(과잉 노출 방지).
 *
 * <p>과거에는 이 조회가 write 포트({@code MemberRepository#findById})로 회원 애그리거트를 로드해 필드를
 * 꺼내는 형태였고, 그 탓에 {@code MemberQueryService}가 write 포트를 들고 있어야 해 CQRS 교차 주입 금지
 * 규칙의 예외로 남아 있었다.
 *
 * <p>등급·상태·성별은 응답까지 그대로 전달되는 표현용이라 도메인 enum이 아니라 이름 문자열로 담는다.
 */
public record MemberManagementDetailResult(
    Long id,
    String username,
    String nickname,
    String fullName,
    String phoneNumber,
    String gender,
    Integer birthDate,
    String memberGrade,
    String memberStatus,
    String statusMessage,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled,
    LocalDateTime createdAt
) {
}
