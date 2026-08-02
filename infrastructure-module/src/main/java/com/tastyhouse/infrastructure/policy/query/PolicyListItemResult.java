package com.tastyhouse.infrastructure.policy.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.policy.model.PolicyType;

/**
 * 정책 문서 목록 항목 조회 결과.
 *
 * <p>버전 이력 목록을 표시하는 용도라 본문(content)은 담지 않는다. 상세용 형제인
 * {@link PolicyDocumentResult}와 같은 패키지에 공존하지만 필드 셋이 달라 통합하지 않는다.
 */
public record PolicyListItemResult(
    Long id,
    PolicyType type,
    String version,
    String title,
    boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {

    @QueryProjection
    public PolicyListItemResult {
    }
}
