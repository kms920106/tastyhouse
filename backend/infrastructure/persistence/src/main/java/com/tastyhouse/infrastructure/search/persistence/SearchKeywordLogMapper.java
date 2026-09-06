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

    static SearchKeywordLogJpaEntity toEntity(SearchKeywordLog domain) {
        return SearchKeywordLogJpaEntity.create(
            domain.getKeyword(),
            domain.getSearchedAt()
        );
    }
}
