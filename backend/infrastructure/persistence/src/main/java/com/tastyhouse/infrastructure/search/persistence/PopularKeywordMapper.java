package com.tastyhouse.infrastructure.search.persistence;

import com.tastyhouse.domain.search.model.PopularKeyword;

final class PopularKeywordMapper {
    private PopularKeywordMapper() {
    }

    static PopularKeyword toDomain(PopularKeywordJpaEntity entity) {
        return PopularKeyword.reconstitute(
            entity.getId(),
            entity.getKeyword(),
            entity.getRank(),
            entity.isNewKeyword(),
            entity.isVisible()
        );
    }

    static PopularKeywordJpaEntity toEntity(PopularKeyword domain) {
        return PopularKeywordJpaEntity.create(
            domain.getKeyword(),
            domain.getRank(),
            domain.isNewKeyword(),
            domain.isVisible()
        );
    }
}
