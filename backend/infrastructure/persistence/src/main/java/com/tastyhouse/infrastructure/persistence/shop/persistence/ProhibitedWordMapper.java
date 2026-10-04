package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.tastyhouse.domain.shop.model.ProhibitedWord;

final class ProhibitedWordMapper {

    private ProhibitedWordMapper() {
    }

    static ProhibitedWord toDomain(ProhibitedWordJpaEntity entity) {
        return ProhibitedWord.reconstitute(
            entity.getId(),
            entity.getWord(),
            entity.getReason()
        );
    }
}
