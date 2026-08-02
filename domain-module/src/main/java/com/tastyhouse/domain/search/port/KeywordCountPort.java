package com.tastyhouse.domain.search.port;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인기 검색어 집계용 검색 키워드 수 조회 출력 포트.
 *
 * <p>인기 검색어 갱신({@code PopularKeywordRefreshService})은 "최근 기간 내 키워드별 검색 수"를 입력으로
 * 순위를 매기는데, 그 집계 조회는 검색 로그 테이블에 대한 그룹 투영이라 도메인이 직접 알 수 없다.
 * 도메인 서비스가 프레임워크·infra를 모르는 상태를 유지하도록 이 포트를 도메인에 두고,
 * infrastructure-module의 어댑터가 구현한다({@code rank}의 {@code MemberReviewCountPort} 선례).
 *
 * <p>반환 순서는 구현이 보장한다 — 검색 수 내림차순 상위 10건. 도메인 서비스는 이 순서를 그대로
 * 순위(1위부터)로 사용한다.
 */
public interface KeywordCountPort {

    List<KeywordCount> findTopKeywordsSince(LocalDateTime since);
}
