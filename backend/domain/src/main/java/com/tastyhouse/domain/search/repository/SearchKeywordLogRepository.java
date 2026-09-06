package com.tastyhouse.domain.search.repository;

import java.time.LocalDateTime;

import com.tastyhouse.domain.search.model.SearchKeywordLog;

public interface SearchKeywordLogRepository {
    SearchKeywordLog save(SearchKeywordLog log);

    void deleteOlderThan(LocalDateTime before);
}
