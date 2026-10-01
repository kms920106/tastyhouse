package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;

final class CeoReplyPhraseMapper {

    private CeoReplyPhraseMapper() {
    }

    static CeoReplyPhrase toDomain(CeoReplyPhraseJpaEntity entity) {
        return CeoReplyPhrase.reconstitute(
            entity.getId(),
            entity.getCeoId() == null ? null : CeoId.of(entity.getCeoId()),
            entity.getName(),
            entity.getContent(),
            entity.getSort(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static CeoReplyPhraseJpaEntity toEntity(CeoReplyPhrase phrase) {
        return CeoReplyPhraseJpaEntity.create(
            phrase.getCeoId() == null ? null : phrase.getCeoId().value(),
            phrase.getName(),
            phrase.getContent(),
            phrase.getSort()
        );
    }

    static void applyChanges(CeoReplyPhraseJpaEntity entity, CeoReplyPhrase phrase) {
        entity.applyChanges(phrase.getName(), phrase.getContent());
    }
}
