package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.search.domain.port.KeywordCount;
import com.tastyhouse.domain.search.domain.port.KeywordCountPort;
import com.tastyhouse.infrastructure.search.query.KeywordCountResult;
import com.tastyhouse.infrastructure.search.query.SearchQueryDao;

/**
 * 인기 검색어 집계용 키워드 수 조회 포트({@link KeywordCountPort}) 어댑터.
 *
 * <p>집계 조회 자체는 {@link SearchQueryDao}에 두고, 이 어댑터는 그 결과를 도메인이 이해하는 값 타입
 * ({@link KeywordCount})으로 옮겨 담는 변환만 담당한다({@code rank}의 {@code MemberReviewCountAdapter}
 * 선례). 덕분에 도메인 서비스는 인프라 투영 형식이나 QueryDSL을 알지 않는다.
 */
@Component
public class KeywordCountAdapter implements KeywordCountPort {

    private final SearchQueryDao searchQueryDao;

    public KeywordCountAdapter(SearchQueryDao searchQueryDao) {
        this.searchQueryDao = searchQueryDao;
    }

    @Override
    public List<KeywordCount> findTopKeywordsSince(LocalDateTime since) {
        return searchQueryDao.findTopKeywordsSince(since).stream()
            .map(this::toKeywordCount)
            .toList();
    }

    private KeywordCount toKeywordCount(KeywordCountResult result) {
        return KeywordCount.of(
            result.keyword(),
            result.count()
        );
    }
}
