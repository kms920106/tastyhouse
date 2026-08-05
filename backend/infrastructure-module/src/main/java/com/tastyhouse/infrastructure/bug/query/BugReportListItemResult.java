package com.tastyhouse.infrastructure.bug.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;

/**
 * 버그 제보 관리 목록 항목 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 첨부 이미지는 목록에서
 * 개수만 필요하므로 서브쿼리 count로 투영한다(상세는 {@link BugReportDetailResult}가 파일 ID 목록을 담는다).
 *
 * <p>{@code memberId}는 소비 모듈이 회원 요약 정보를 별도 조회하는 키로 쓰지만, query 계층 규약대로
 * raw {@code Long}으로 투영한다(소비 측에서 필요하면 VO로 승격한다).
 */
public record BugReportListItemResult(
    Long id,
    Long memberId,
    String device,
    String title,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority,
    long imageCount,
    LocalDateTime createdAt
) {

    @QueryProjection
    public BugReportListItemResult {
    }
}
