package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ProhibitedWordState;
import com.tastyhouse.domain.shop.model.ProhibitedWord;

final class ProhibitedWordStateMapper {
    private ProhibitedWordStateMapper() {
    }

    static ProhibitedWord toDomain(ProhibitedWordState state) {
        return ProhibitedWord.reconstitute(
            state.id(),
            state.word(),
            state.reason()
        );
    }

    static ProhibitedWordState toState(ProhibitedWord prohibitedWord) {
        return new ProhibitedWordState(
            prohibitedWord.getId(),
            prohibitedWord.getWord(),
            prohibitedWord.getReason()
        );
    }
}
