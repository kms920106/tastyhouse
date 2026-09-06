package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.search.port.KeywordCount;
import com.tastyhouse.domain.search.port.KeywordCountPort;
import com.tastyhouse.application.search.port.out.KeywordCountResult;
import com.tastyhouse.infrastructure.search.query.SearchQueryDao;

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
