package com.tastyhouse.core.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientTag() {
        Tag tag = Tag.of("맛집");

        assertThat(tag.getId()).isNull();
        assertThat(tag.getTagName()).isEqualTo("맛집");
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        Tag tag = Tag.reconstitute(1L, "맛집");

        assertThat(tag.getId()).isEqualTo(1L);
        assertThat(tag.getTagName()).isEqualTo("맛집");
    }
}
