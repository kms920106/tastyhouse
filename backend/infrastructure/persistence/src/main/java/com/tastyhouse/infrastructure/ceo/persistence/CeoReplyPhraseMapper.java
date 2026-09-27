package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseState;

final class CeoReplyPhraseMapper {
    private CeoReplyPhraseMapper() {
    }

    static CeoReplyPhraseState toState(CeoReplyPhraseJpaEntity entity) {
        return new CeoReplyPhraseState(
            entity.getId(),
            entity.getCeoId(),
            entity.getName(),
            entity.getContent(),
            entity.getSort(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static CeoReplyPhraseJpaEntity toEntity(CeoReplyPhraseState state) {
        return CeoReplyPhraseJpaEntity.create(
            state.ceoId(),
            state.name(),
            state.content(),
            state.sort()
        );
    }

    static void applyChanges(CeoReplyPhraseJpaEntity entity, CeoReplyPhraseState state) {
        entity.applyChanges(state.name(), state.content());
    }
}
