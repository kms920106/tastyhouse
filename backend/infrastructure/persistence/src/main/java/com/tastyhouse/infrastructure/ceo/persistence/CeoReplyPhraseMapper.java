package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class CeoReplyPhraseMapper {
    private CeoReplyPhraseMapper() {
    }

    static CeoReplyPhrase toDomain(CeoReplyPhraseJpaEntity entity) {
        return CeoReplyPhrase.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getName(),
            entity.getContent(),
            entity.getSort(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static CeoReplyPhraseJpaEntity toEntity(CeoReplyPhrase domain) {
        return CeoReplyPhraseJpaEntity.create(
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getName(),
            domain.getContent(),
            domain.getSort()
        );
    }

    static void applyChanges(CeoReplyPhraseJpaEntity entity, CeoReplyPhrase domain) {
        entity.applyChanges(domain.getName(), domain.getContent());
    }
}
