package com.tastyhouse.application.search.port.out.write;

import java.time.LocalDateTime;

public interface SearchKeywordLogStatePort {
    SearchKeywordLogState save(SearchKeywordLogState state);

    void deleteOlderThan(LocalDateTime before);
}
