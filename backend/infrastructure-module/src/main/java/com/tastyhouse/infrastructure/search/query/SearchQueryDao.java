package com.tastyhouse.infrastructure.search.query;

import com.tastyhouse.application.search.port.out.SearchQueryPort;
import com.tastyhouse.application.search.port.out.KeywordCountResult;
import com.tastyhouse.application.search.port.out.PopularKeywordResult;
import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;
import com.querydsl.core.types.Projections;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.search.persistence.QPopularKeywordJpaEntity.popularKeywordJpaEntity;
import static com.tastyhouse.infrastructure.search.persistence.QRecommendedKeywordJpaEntity.recommendedKeywordJpaEntity;
import static com.tastyhouse.infrastructure.search.persistence.QSearchKeywordLogJpaEntity.searchKeywordLogJpaEntity;

/**
 * 검색 키워드 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code PopularKeywordRepository})와 역할이 겹치지 않는다. 추천 검색어는 이 DAO의 조회가
 * 유일한 접근 경로라(읽기 전용 애그리거트) write 포트 자체가 없다. 소비 모듈(web-api)의
 * {@code SearchQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 인기/추천 두 애그리거트의 조회 메서드를 이 한 클래스에 둔다. 검색
 * 키워드 조회는 web 노출 전용이라 admin 소비자가 없어 메서드가 각각 하나씩만 있다.
 */
@Repository
public class SearchQueryDao implements SearchQueryPort {

    /**
     * 인기 검색어로 노출하는 상위 키워드 수.
     */
    private static final long TOP_KEYWORD_LIMIT = 10L;

    private final JPAQueryFactory queryFactory;

    public SearchQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 노출 인기 검색어 목록 조회 — 노출(visible=true) 항목만 순위 오름차순으로 조회한다.
     */
    @Override
    public List<PopularKeywordResult> findVisiblePopularKeywords() {
        return queryFactory
            .select(Projections.constructor(PopularKeywordResult.class,
                popularKeywordJpaEntity.rank,
                popularKeywordJpaEntity.keyword,
                popularKeywordJpaEntity.newKeyword
            ))
            .from(popularKeywordJpaEntity)
            .where(popularKeywordJpaEntity.visible.isTrue())
            .orderBy(popularKeywordJpaEntity.rank.asc())
            .fetch();
    }

    /**
     * 노출 추천 검색어 목록 조회 — 노출(visible=true) 항목만 정렬 순서 오름차순으로 조회한다.
     */
    @Override
    public List<RecommendedKeywordResult> findVisibleRecommendedKeywords() {
        return queryFactory
            .select(Projections.constructor(RecommendedKeywordResult.class,
                recommendedKeywordJpaEntity.keyword))
            .from(recommendedKeywordJpaEntity)
            .where(recommendedKeywordJpaEntity.visible.isTrue())
            .orderBy(recommendedKeywordJpaEntity.sortOrder.asc())
            .fetch();
    }

    /**
     * 집계 기준 시각 이후 검색된 키워드를 횟수 내림차순 상위 10건까지 집계한다.
     *
     * <p>인기 검색어 갱신(도메인 서비스)이 이 집계를 입력으로 순위를 매긴다 — 소비자가 도메인이라
     * 결과는 {@code SearchKeywordCountAdapter}가 도메인 값 타입으로 옮겨 담아 전달한다.
     */
    @Override
    public List<KeywordCountResult> findTopKeywordsSince(LocalDateTime since) {
        return queryFactory
            .select(Projections.constructor(KeywordCountResult.class,
                searchKeywordLogJpaEntity.keyword,
                searchKeywordLogJpaEntity.count()
            ))
            .from(searchKeywordLogJpaEntity)
            .where(searchKeywordLogJpaEntity.searchedAt.goe(since))
            .groupBy(searchKeywordLogJpaEntity.keyword)
            .orderBy(searchKeywordLogJpaEntity.count().desc())
            .limit(TOP_KEYWORD_LIMIT)
            .fetch();
    }
}
