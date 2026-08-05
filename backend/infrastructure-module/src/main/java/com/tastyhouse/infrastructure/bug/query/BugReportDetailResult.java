package com.tastyhouse.infrastructure.bug.query;

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
 * 없다. 따라서 스칼라 필드는 {@link BugReportDetailProjection}({@code @QueryProjection})으로 투영하고,
 * 이미지는 DAO가 두 번째 조회(BUG_REPORT_IMAGE ⋈ UPLOADED_FILE)로 파일명·URL까지 모아 이 record의
 * {@code from} 팩토리로 합친다(소비 측의 추가 파일 조회 제거).
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

    public static BugReportDetailResult from(BugReportDetailProjection projection, List<BugReportImageResult> images) {
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
            images,
            projection.createdAt(),
            projection.updatedAt()
        );
    }
}
