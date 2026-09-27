package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopTemporaryClosureMapperTest {

    @Test
    @DisplayName("ShopTemporaryClosure 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopTemporaryClosure() {
        ShopTemporaryClosure original = ShopTemporaryClosure.reconstitute(
            185L,
            ShopId.of(186L),
            LocalDate.of(2026, 2, 4),
            LocalDate.of(2026, 2, 5),
            LocalDateTime.of(2026, 1, 6, 10, 29)
        );

        ShopTemporaryClosureJpaEntity entity = ShopTemporaryClosureMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(186L);
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 4));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2026, 2, 5));
    }

    @Test
    @DisplayName("ShopTemporaryClosure 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopTemporaryClosure() {
        ShopTemporaryClosure original = ShopTemporaryClosure.reconstitute(
            185L,
            ShopId.of(186L),
            LocalDate.of(2026, 2, 4),
            LocalDate.of(2026, 2, 5),
            LocalDateTime.of(2026, 1, 6, 10, 29)
        );

        ShopTemporaryClosureJpaEntity entity = ShopTemporaryClosureJpaEntity.create(
            186L,
            LocalDate.of(2026, 2, 4),
            LocalDate.of(2026, 2, 5)
        );
        ReflectionTestUtils.setField(entity, "id", 185L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 6, 10, 29));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopTemporaryClosureMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
