package com.tastyhouse.infrastructure.search.persistence;

import com.tastyhouse.application.search.port.out.write.SearchKeywordLogState;

final class SearchKeywordLogMapper {
    private SearchKeywordLogMapper() {
    }

    static SearchKeywordLogState toState(SearchKeywordLogJpaEntity entity) {
        return new SearchKeywordLogState(
            entity.getId(),
            entity.getKeyword(),
            entity.getSearchedAt()
        );
    }

    static SearchKeywordLogJpaEntity toEntity(SearchKeywordLogState state) {
        return SearchKeywordLogJpaEntity.create(
            state.keyword(),
            state.searchedAt()
        );
    }
}
