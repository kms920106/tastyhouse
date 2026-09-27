package com.tastyhouse.infrastructure.search.persistence;

import com.tastyhouse.domain.search.model.SearchKeywordLog;

final class SearchKeywordLogMapper {
    private SearchKeywordLogMapper() {
    }

    static SearchKeywordLog toDomain(SearchKeywordLogJpaEntity entity) {
        return SearchKeywordLog.reconstitute(
            entity.getId(),
            entity.getKeyword(),
            entity.getSearchedAt()
        );
    }

    static SearchKeywordLogJpaEntity toEntity(SearchKeywordLog searchKeywordLog) {
        return SearchKeywordLogJpaEntity.create(
            searchKeywordLog.getKeyword(),
            searchKeywordLog.getSearchedAt()
        );
    }
}
