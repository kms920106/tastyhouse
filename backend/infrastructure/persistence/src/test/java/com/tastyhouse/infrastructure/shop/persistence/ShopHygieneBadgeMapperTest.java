package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopHygieneBadgeMapperTest {

    @Test
    @DisplayName("ShopHygieneBadge 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopHygieneBadge() {
        ShopHygieneBadge original = ShopHygieneBadge.reconstitute(
            148L,
            ShopId.of(149L),
            HygieneBadgeType.CESCO_WHITE,
            LocalDate.of(2026, 2, 24),
            "v52",
            LocalDateTime.of(2026, 1, 26, 10, 53)
        );

        ShopHygieneBadgeJpaEntity entity = ShopHygieneBadgeMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(149L);
        assertThat(entity.getBadgeType()).isEqualTo("CESCO_WHITE");
        assertThat(entity.getCertifiedDate()).isEqualTo(LocalDate.of(2026, 2, 24));
        assertThat(entity.getLastInspectionMonth()).isEqualTo("v52");
    }

    @Test
    @DisplayName("ShopHygieneBadge 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopHygieneBadge() {
        ShopHygieneBadge original = ShopHygieneBadge.reconstitute(
            148L,
            ShopId.of(149L),
            HygieneBadgeType.CESCO_WHITE,
            LocalDate.of(2026, 2, 24),
            "v52",
            LocalDateTime.of(2026, 1, 26, 10, 53)
        );

        ShopHygieneBadgeJpaEntity entity = ShopHygieneBadgeJpaEntity.create(
            149L,
            "CESCO_WHITE",
            LocalDate.of(2026, 2, 24),
            "v52"
        );
        ReflectionTestUtils.setField(entity, "id", 148L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 26, 10, 53));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopHygieneBadgeMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
