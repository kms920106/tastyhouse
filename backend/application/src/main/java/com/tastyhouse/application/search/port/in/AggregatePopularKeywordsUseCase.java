package com.tastyhouse.application.search.port.in;

import com.tastyhouse.application.shared.marker.BatchApp;

@BatchApp
public interface AggregatePopularKeywordsUseCase {

    void aggregatePopularKeywords();

    void deleteOldSearchLogs();
}
