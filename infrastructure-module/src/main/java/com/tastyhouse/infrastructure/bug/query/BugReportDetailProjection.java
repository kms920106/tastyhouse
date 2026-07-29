package com.tastyhouse.infrastructure.bug.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 버그 제보 상세의 스칼라 필드 QueryDSL 투영 전용 record.
 *
 * <p>{@link BugReportDetailResult}는 별도 테이블에서 모으는 {@code imageFileIds} 목록을 포함하므로
 * 한 번의 투영으로 만들 수 없다. 이 record가 BUG_REPORT 한 행의 스칼라 필드만 받고, DAO가 이미지 ID를
 * 별도 조회로 합쳐 최종 결과를 조립한다. DAO 내부 조립 단계 전용이며 소비 모듈에 노출되지 않는다.
 */
public record BugReportDetailProjection(
    Long id,
    MemberId memberId,
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

    @QueryProjection
    public BugReportDetailProjection {
    }
}
