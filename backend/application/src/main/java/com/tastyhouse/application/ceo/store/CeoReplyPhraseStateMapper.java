package com.tastyhouse.application.ceo.store;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseState;

final class CeoReplyPhraseStateMapper {
    private CeoReplyPhraseStateMapper() {
    }

    static CeoReplyPhrase toDomain(CeoReplyPhraseState state) {
        return CeoReplyPhrase.reconstitute(
            state.id(),
            state.ceoId() == null ? null : CeoId.of(state.ceoId()),
            state.name(),
            state.content(),
            state.sort(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static CeoReplyPhraseState toState(CeoReplyPhrase phrase) {
        return new CeoReplyPhraseState(
            phrase.getId(),
            phrase.getCeoId() == null ? null : phrase.getCeoId().value(),
            phrase.getName(),
            phrase.getContent(),
            phrase.getSort(),
            phrase.getCreatedAt(),
            phrase.getUpdatedAt()
        );
    }
}
