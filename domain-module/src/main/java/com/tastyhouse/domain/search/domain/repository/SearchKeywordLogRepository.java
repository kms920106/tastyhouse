package com.tastyhouse.domain.search.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.search.domain.model.SearchKeywordLog;

public interface SearchKeywordLogRepository {

    SearchKeywordLog save(SearchKeywordLog log);

    List<Object[]> findTop10KeywordsSince(LocalDateTime since);

    void deleteOlderThan(LocalDateTime before);
}
