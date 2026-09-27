package com.tastyhouse.application.search.store;

import com.tastyhouse.application.search.port.out.write.SearchKeywordLogState;
import com.tastyhouse.domain.search.model.SearchKeywordLog;

final class SearchKeywordLogStateMapper {
    private SearchKeywordLogStateMapper() {
    }

    static SearchKeywordLog toDomain(SearchKeywordLogState state) {
        return SearchKeywordLog.reconstitute(
            state.id(),
            state.keyword(),
            state.searchedAt()
        );
    }

    static SearchKeywordLogState toState(SearchKeywordLog searchKeywordLog) {
        return new SearchKeywordLogState(
            searchKeywordLog.getId(),
            searchKeywordLog.getKeyword(),
            searchKeywordLog.getSearchedAt()
        );
    }
}
