package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopSuspensionMapperTest {

    @Test
    @DisplayName("ShopSuspension 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopSuspension() {
        ShopSuspension original = ShopSuspension.reconstitute(
            176L,
            ShopId.of(177L),
            SuspensionReason.EARLY_CLOSE,
            OrderMethod.TAKEOUT,
            LocalDateTime.of(2026, 1, 25, 10, 20),
            LocalDateTime.of(2026, 1, 26, 10, 21),
            LocalDateTime.of(2026, 1, 27, 10, 22),
            LocalDateTime.of(2026, 1, 28, 10, 23),
            LocalDateTime.of(2026, 1, 1, 10, 24)
        );

        ShopSuspensionJpaEntity entity = ShopSuspensionMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(177L);
        assertThat(entity.getReason()).isEqualTo("EARLY_CLOSE");
        assertThat(entity.getOrderMethod()).isEqualTo("TAKEOUT");
        assertThat(entity.getStartAt()).isEqualTo(LocalDateTime.of(2026, 1, 25, 10, 20));
        assertThat(entity.getEndAt()).isEqualTo(LocalDateTime.of(2026, 1, 26, 10, 21));
        assertThat(entity.getReleasedAt()).isEqualTo(LocalDateTime.of(2026, 1, 27, 10, 22));
    }

    @Test
    @DisplayName("ShopSuspension 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopSuspension() {
        ShopSuspension original = ShopSuspension.reconstitute(
            176L,
            ShopId.of(177L),
            SuspensionReason.EARLY_CLOSE,
            OrderMethod.TAKEOUT,
            LocalDateTime.of(2026, 1, 25, 10, 20),
            LocalDateTime.of(2026, 1, 26, 10, 21),
            LocalDateTime.of(2026, 1, 27, 10, 22),
            LocalDateTime.of(2026, 1, 28, 10, 23),
            LocalDateTime.of(2026, 1, 1, 10, 24)
        );

        ShopSuspensionJpaEntity entity = ShopSuspensionJpaEntity.create(
            177L,
            "EARLY_CLOSE",
            "TAKEOUT",
            LocalDateTime.of(2026, 1, 25, 10, 20),
            LocalDateTime.of(2026, 1, 26, 10, 21),
            LocalDateTime.of(2026, 1, 27, 10, 22)
        );
        ReflectionTestUtils.setField(entity, "id", 176L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 28, 10, 23));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 1, 10, 24));

        assertThat(ShopSuspensionMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
