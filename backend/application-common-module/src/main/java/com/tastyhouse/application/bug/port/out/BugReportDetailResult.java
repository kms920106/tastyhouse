package com.tastyhouse.application.bug.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;

/**
 * 버그 제보 관리 상세 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 목록용 형제인
 * {@link BugReportListItemResult}와 달리 본문·처리 이력·첨부 이미지 목록까지 담는다.
 *
 * <p>{@code images}는 별도 테이블(BUG_REPORT_IMAGE)의 다건이라 단일 QueryDSL 투영으로 채울 수
 * 없다. 스칼라 필드 투영과 이미지 2차 조회(BUG_REPORT_IMAGE ⋈ UPLOADED_FILE)를 합쳐 이 record를
 * 조립하는 일은 어댑터({@code BugReportQueryDao})의 몫이다 — 조립에 쓰는 중간 투영은 infra 내부
 * 타입이므로, 그 팩토리를 이 포트 DTO에 두면 응용 계층이 infra를 참조하게 된다.
 *
 * <p>{@code memberId}는 소비 모듈이 회원 요약 정보를 별도 조회하는 키로 쓰지만, query 계층 규약대로
 * raw {@code Long}으로 유지한다(소비 측에서 필요하면 VO로 승격한다).
 */
public record BugReportDetailResult(
    Long id,
    Long memberId,
    String device,
    String title,
    String content,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority,
    Long assigneeAdminId,
    String adminAnswer,
    LocalDateTime resolvedAt,
    String appVersion,
    BugReportPlatform platform,
    String osVersion,
    List<BugReportImageResult> images,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
