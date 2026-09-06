package com.tastyhouse.domain.search.port;

/**
 * 인기 검색어 집계 입력값 — 집계 기간 동안 한 키워드가 검색된 횟수.
 *
 * <p>{@link KeywordCountPort}의 반환 타입으로, 도메인 계층이 소유하는 순수 값 객체다. 집계 조회
 * (그룹 투영)는 infrastructure-module이 담당하며, 그 결과를 이 타입으로 옮겨 담아 도메인에 넘긴다.
 * 덕분에 도메인 서비스는 인프라 투영 형식({@code Object[]} 튜플)에 결합되지 않는다.
 */
public record KeywordCount(
    String keyword,
    long count
) {

    public static KeywordCount of(
        String keyword,
        long count
    ) {
        return new KeywordCount(
            keyword,
            count
        );
    }
}
