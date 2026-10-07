package com.tastyhouse.application.search.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.search.port.in.SearchRecommendedKeywordQueryUseCase;
import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;
import com.tastyhouse.application.search.port.out.SearchQueryPort;

@Service
@Transactional(readOnly = true)
class SearchRecommendedKeywordQueryService implements SearchRecommendedKeywordQueryUseCase {

    private final SearchQueryPort searchQueryPort;

    public SearchRecommendedKeywordQueryService(SearchQueryPort searchQueryPort) {
        this.searchQueryPort = searchQueryPort;
    }

    @Override
    public List<RecommendedKeywordResult> getRecommendedKeywords() {
        return searchQueryPort.findVisibleRecommendedKeywords();
    }
}
