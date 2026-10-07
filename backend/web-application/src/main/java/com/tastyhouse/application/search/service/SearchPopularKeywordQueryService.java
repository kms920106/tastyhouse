package com.tastyhouse.application.search.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.search.port.in.SearchPopularKeywordQueryUseCase;
import com.tastyhouse.application.search.port.out.PopularKeywordResult;
import com.tastyhouse.application.search.port.out.SearchQueryPort;

@Service
@Transactional(readOnly = true)
class SearchPopularKeywordQueryService implements SearchPopularKeywordQueryUseCase {

    private final SearchQueryPort searchQueryPort;

    public SearchPopularKeywordQueryService(SearchQueryPort searchQueryPort) {
        this.searchQueryPort = searchQueryPort;
    }

    @Override
    public List<PopularKeywordResult> getPopularKeywords() {
        return searchQueryPort.findVisiblePopularKeywords();
    }
}
