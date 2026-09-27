package com.tastyhouse.application.shop.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ProhibitedWord;

import static org.assertj.core.api.Assertions.assertThat;

class ProhibitedWordStateMapperTest {

    @Test
    @DisplayName("ProhibitedWord → ProhibitedWordState → ProhibitedWord 왕복 시 모든 필드가 보존된다")
    void prohibitedWordRoundTrip() {
        ProhibitedWord original = ProhibitedWord.reconstitute(
            101L,
            "v2",
            "v3"
        );

        ProhibitedWord restored = ProhibitedWordStateMapper.toDomain(ProhibitedWordStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
