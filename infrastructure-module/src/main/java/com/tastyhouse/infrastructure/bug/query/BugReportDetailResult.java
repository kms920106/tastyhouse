package com.tastyhouse.infrastructure.bug.query;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 버그 제보 관리 상세 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 목록용 형제인
 * {@link BugReportListItemResult}와 달리 본문·처리 이력·첨부 파일 ID 목록까지 담는다.
 *
 * <p>{@code imageFileIds}는 별도 테이블(BUG_REPORT_IMAGE)의 다건이라 단일 QueryDSL 투영으로 채울 수
 * 없다. 따라서 스칼라 필드는 {@link BugReportDetailProjection}({@code @QueryProjection})으로 투영하고,
 * 이미지 ID는 DAO가 두 번째 조회로 모아 이 record의 {@code from} 팩토리로 합친다.
 *
 * <p>{@code memberId}는 소비 모듈이 회원 요약 정보를 별도 조회하는 키로 쓰므로 도메인 VO
 * {@code MemberId}로 유지한다.
 */
public record BugReportDetailResult(
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
    List<Long> imageFileIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static BugReportDetailResult from(BugReportDetailProjection projection, List<Long> imageFileIds) {
        return new BugReportDetailResult(
            projection.id(),
            projection.memberId(),
            projection.device(),
            projection.title(),
            projection.content(),
            projection.status(),
            projection.category(),
            projection.priority(),
            projection.assigneeAdminId(),
            projection.adminAnswer(),
            projection.resolvedAt(),
            projection.appVersion(),
            projection.platform(),
            projection.osVersion(),
            imageFileIds,
            projection.createdAt(),
            projection.updatedAt()
        );
    }
}
