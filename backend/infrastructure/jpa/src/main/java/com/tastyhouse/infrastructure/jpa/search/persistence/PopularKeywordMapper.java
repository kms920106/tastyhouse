package com.tastyhouse.infrastructure.jpa.search.persistence;

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

    static PopularKeywordJpaEntity toEntity(PopularKeyword popularKeyword) {
        return PopularKeywordJpaEntity.create(
            popularKeyword.getKeyword(),
            popularKeyword.getRank(),
            popularKeyword.isNewKeyword(),
            popularKeyword.isVisible()
        );
    }
}
