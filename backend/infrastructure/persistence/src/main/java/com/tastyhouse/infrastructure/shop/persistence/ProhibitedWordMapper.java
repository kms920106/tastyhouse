package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ProhibitedWordState;

final class ProhibitedWordMapper {
    private ProhibitedWordMapper() {
    }

    static ProhibitedWordState toState(ProhibitedWordJpaEntity entity) {
        return new ProhibitedWordState(
            entity.getId(),
            entity.getWord(),
            entity.getReason()
        );
    }
}
