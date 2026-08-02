package com.tastyhouse.infrastructure.search.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 인기 검색어 목록 항목 조회 결과.
 *
 * <p>노출(visible=true) 인기 검색어만 조회하므로 노출 여부 필드를 갖지 않는다.
 */
public record PopularKeywordResult(
    int rank,
    String keyword,
    boolean newKeyword
) {

    @QueryProjection
    public PopularKeywordResult {
    }
}
