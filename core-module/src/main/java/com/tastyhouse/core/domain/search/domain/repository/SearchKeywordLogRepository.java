package com.tastyhouse.core.domain.search.domain.repository;

import com.tastyhouse.core.domain.search.domain.model.SearchKeywordLog;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchKeywordLogRepository {

    SearchKeywordLog save(SearchKeywordLog log);

    List<Object[]> findTop10KeywordsSince(LocalDateTime since);

    void deleteOlderThan(LocalDateTime before);
}
