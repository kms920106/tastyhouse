package com.tastyhouse.infrastructure.search.persistence;

import com.tastyhouse.application.search.port.out.write.PopularKeywordState;

final class PopularKeywordMapper {
    private PopularKeywordMapper() {
    }

    static PopularKeywordState toState(PopularKeywordJpaEntity entity) {
        return new PopularKeywordState(
            entity.getId(),
            entity.getKeyword(),
            entity.getRank(),
            entity.isNewKeyword(),
            entity.isVisible()
        );
    }

    static PopularKeywordJpaEntity toEntity(PopularKeywordState state) {
        return PopularKeywordJpaEntity.create(
            state.keyword(),
            state.rank(),
            state.newKeyword(),
            state.visible()
        );
    }
}
