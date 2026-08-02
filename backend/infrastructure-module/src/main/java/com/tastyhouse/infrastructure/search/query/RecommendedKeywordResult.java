package com.tastyhouse.infrastructure.search.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 추천 검색어 목록 항목 조회 결과.
 *
 * <p>노출(visible=true) 추천 검색어만 정렬 순서대로 조회하므로, 소비 모듈이 실제로 쓰는 검색어 하나만 담는다.
 */
public record RecommendedKeywordResult(
    String keyword
) {

    @QueryProjection
    public RecommendedKeywordResult {
    }
}
