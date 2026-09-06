package com.tastyhouse.application.search.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.search.port.in.AggregatePopularKeywordsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.search.service.PopularKeywordRefreshService;

@Service
@BatchApp
@Transactional
public class SearchKeywordSchedulerService implements AggregatePopularKeywordsUseCase {

    private final PopularKeywordRefreshService popularKeywordRefreshService;

    public SearchKeywordSchedulerService(PopularKeywordRefreshService popularKeywordRefreshService) {
        this.popularKeywordRefreshService = popularKeywordRefreshService;
    }

    @Override
    public void aggregatePopularKeywords() {
        popularKeywordRefreshService.refresh();
    }

    @Override
    public void deleteOldSearchLogs() {
        popularKeywordRefreshService.deleteOldSearchLogs();
    }
}
