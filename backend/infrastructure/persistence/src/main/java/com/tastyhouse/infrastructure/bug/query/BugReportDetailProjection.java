package com.tastyhouse.infrastructure.bug.query;

import com.tastyhouse.application.bug.port.out.BugReportDetailResult;
import java.time.LocalDateTime;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;

/**
 * 버그 제보 상세의 스칼라 필드 QueryDSL 투영 전용 record.
 *
 * <p>{@link BugReportDetailResult}는 별도 테이블에서 모으는 {@code imageFileIds} 목록을 포함하므로
 * 한 번의 투영으로 만들 수 없다. 이 record가 BUG_REPORT 한 행의 스칼라 필드만 받고, DAO가 이미지 ID를
 * 별도 조회로 합쳐 최종 결과를 조립한다. DAO 내부 조립 단계 전용이며 소비 모듈에 노출되지 않는다.
 *
 * <p>어댑터 내부 타입이라 {@code application-common-module}로 옮기지 않았지만, {@code @QueryProjection}은
 * 쓰지 않는다 — 그 어노테이션이 리포에 하나라도 남으면 "읽기 투영은 {@code Projections.constructor}로
 * 한다"는 규칙에 예외가 생기고, Q타입 생성물이 다시 늘어난다. {@code Projections.constructor}가
 * <b>public 생성자만</b> 탐색하므로 이 record도 {@code public}을 유지해야 한다.
 */
public record BugReportDetailProjection(
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
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
