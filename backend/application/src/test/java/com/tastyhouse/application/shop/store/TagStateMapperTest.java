package com.tastyhouse.application.shop.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.Tag;

import static org.assertj.core.api.Assertions.assertThat;

class TagStateMapperTest {

    @Test
    @DisplayName("Tag → TagState → Tag 왕복 시 모든 필드가 보존된다")
    void tagRoundTrip() {
        Tag original = Tag.reconstitute(
            168L,
            "v69"
        );

        Tag restored = TagStateMapper.toDomain(TagStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
