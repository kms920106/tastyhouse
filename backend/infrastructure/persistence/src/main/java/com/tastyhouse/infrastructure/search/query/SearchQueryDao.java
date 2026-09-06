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

@Repository
public class SearchQueryDao implements SearchQueryPort {
    private static final long TOP_KEYWORD_LIMIT = 10L;

    private final JPAQueryFactory queryFactory;

    public SearchQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

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
