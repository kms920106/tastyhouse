package com.tastyhouse.application.search.port.in;

public interface AggregatePopularKeywordsUseCase {

    void aggregatePopularKeywords();

    void deleteOldSearchLogs();
}
