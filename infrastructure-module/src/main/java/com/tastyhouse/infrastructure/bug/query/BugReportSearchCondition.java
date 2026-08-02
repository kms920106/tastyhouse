package com.tastyhouse.infrastructure.bug.query;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 버그 제보 관리 목록 검색 조건.
 *
 * <p>표현 목적 read의 입력이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다.
 * 소비 모듈(admin-api)의 {@code BugReportQueryService}가 원시 파라미터를 받아
 * {@code BugReportStatus.from(String)} 등으로 승격한 뒤 조립해 전달한다(api 모듈은 core enum을
 * HTTP 경계에 노출하지 않는다).
 */
public record BugReportSearchCondition(
    String title,
    String content,
    MemberId memberId,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority
) {

    public static BugReportSearchCondition of(
        String title,
        String content,
        MemberId memberId,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority
    ) {
        return new BugReportSearchCondition(title, content, memberId, status, category, priority);
    }
}
