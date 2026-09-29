package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.search.port.out.KeywordCount;
import com.tastyhouse.application.search.port.out.KeywordCountPort;
import com.tastyhouse.application.search.port.out.KeywordCountResult;
import com.tastyhouse.infrastructure.search.query.SearchQueryAdapter;

@Component
public class KeywordCountAdapter implements KeywordCountPort {
    private final SearchQueryAdapter searchQueryAdapter;

    public KeywordCountAdapter(SearchQueryAdapter searchQueryAdapter) {
        this.searchQueryAdapter = searchQueryAdapter;
    }

    @Override
    public List<KeywordCount> findTopKeywordsSince(LocalDateTime since) {
        return searchQueryAdapter.findTopKeywordsSince(since).stream()
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
