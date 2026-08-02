package com.tastyhouse.infrastructure.search.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 집계 기간 내 키워드별 검색 횟수 한 건.
 */
public record KeywordCountResult(
    String keyword,
    long count
) {

    @QueryProjection
    public KeywordCountResult {
    }
}
