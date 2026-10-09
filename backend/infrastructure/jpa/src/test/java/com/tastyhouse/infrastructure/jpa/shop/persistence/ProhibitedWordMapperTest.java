package com.tastyhouse.infrastructure.jpa.shop.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ProhibitedWord;

import static org.assertj.core.api.Assertions.assertThat;

class ProhibitedWordMapperTest {

    @Test
    @DisplayName("ProhibitedWord 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomain() {
        ProhibitedWordJpaEntity entity = new ProhibitedWordJpaEntity();
        ReflectionTestUtils.setField(entity, "id", 101L);
        ReflectionTestUtils.setField(entity, "word", "v2");
        ReflectionTestUtils.setField(entity, "reason", "v3");

        ProhibitedWord domain = ProhibitedWordMapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(101L);
        assertThat(domain.getWord()).isEqualTo("v2");
        assertThat(domain.getReason()).isEqualTo("v3");
    }
}
