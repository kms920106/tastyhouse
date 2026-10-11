package com.tastyhouse.application.search.port.out.write;

import java.time.LocalDateTime;

import com.tastyhouse.domain.search.model.SearchKeywordLog;

public interface SearchKeywordLogSavePort {

    SearchKeywordLog save(SearchKeywordLog log);

    void deleteOlderThan(LocalDateTime before);
}
