package com.tastyhouse.infrastructure.policy.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.policy.model.PolicyType;

/**
 * 정책 문서 상세 조회 결과.
 *
 * <p>본문(content)을 포함한 전체 필드를 담아 상세 화면에 대응한다. 목록용 형제인
 * {@link PolicyListItemResult}와 같은 패키지에 공존하지만 필드 셋이 달라(목록은 본문 제외)
 * 통합하지 않는다. 소비 모듈(web-api)의 {@code PolicyQueryService}만 사용한다 — admin 전용
 * 형제가 없어 {@code Management} 한정어는 붙이지 않는다.
 */
public record PolicyDocumentResult(
    Long id,
    PolicyType type,
    String version,
    String title,
    String content,
    boolean current,
    boolean mandatory,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    @QueryProjection
    public PolicyDocumentResult {
    }
}
