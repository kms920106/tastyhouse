package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopChoiceMapperTest {

    @Test
    @DisplayName("ShopChoice 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopChoice() {
        ShopChoice original = ShopChoice.reconstitute(
            123L,
            ShopId.of(124L),
            "v25",
            "v26"
        );

        ShopChoiceJpaEntity entity = ShopChoiceMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(124L);
        assertThat(entity.getTitle()).isEqualTo("v25");
        assertThat(entity.getContent()).isEqualTo("v26");
    }

    @Test
    @DisplayName("ShopChoice 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopChoice() {
        ShopChoice original = ShopChoice.reconstitute(
            123L,
            ShopId.of(124L),
            "v25",
            "v26"
        );

        ShopChoiceJpaEntity entity = ShopChoiceJpaEntity.create(
            124L,
            "v25",
            "v26"
        );
        ReflectionTestUtils.setField(entity, "id", 123L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopChoiceMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
