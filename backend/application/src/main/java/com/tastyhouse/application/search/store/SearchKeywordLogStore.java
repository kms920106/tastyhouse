package com.tastyhouse.application.search.store;

import java.time.LocalDateTime;

import com.tastyhouse.domain.search.model.SearchKeywordLog;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogStatePort;

public class SearchKeywordLogStore implements SearchKeywordLogRepository {
    private final SearchKeywordLogStatePort searchKeywordLogStatePort;

    public SearchKeywordLogStore(SearchKeywordLogStatePort searchKeywordLogStatePort) {
        this.searchKeywordLogStatePort = searchKeywordLogStatePort;
    }

    @Override
    public SearchKeywordLog save(SearchKeywordLog log) {
        return SearchKeywordLogStateMapper.toDomain(searchKeywordLogStatePort.save(SearchKeywordLogStateMapper.toState(log)));
    }

    @Override
    public void deleteOlderThan(LocalDateTime before) {
        searchKeywordLogStatePort.deleteOlderThan(before);
    }
}
