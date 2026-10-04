package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopNoticeMapperTest {

    @Test
    @DisplayName("ShopNotice 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopNotice() {
        ShopNotice original = ShopNotice.reconstitute(
            101L,
            ShopId.of(102L),
            "v3",
            true,
            false,
            LocalDateTime.of(2026, 1, 7, 10, 6),
            LocalDateTime.of(2026, 1, 8, 10, 7)
        );

        ShopNoticeJpaEntity entity = ShopNoticeMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(102L);
        assertThat(entity.getContent()).isEqualTo("v3");
        assertThat(entity.isExposed()).isEqualTo(true);
        assertThat(entity.isHidden()).isEqualTo(false);
    }

    @Test
    @DisplayName("ShopNotice 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopNotice() {
        ShopNotice original = ShopNotice.reconstitute(
            101L,
            ShopId.of(102L),
            "v3",
            true,
            false,
            LocalDateTime.of(2026, 1, 7, 10, 6),
            LocalDateTime.of(2026, 1, 8, 10, 7)
        );

        ShopNoticeJpaEntity entity = ShopNoticeJpaEntity.create(
            102L,
            "v3",
            true,
            false
        );
        ReflectionTestUtils.setField(entity, "id", 101L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 7, 10, 6));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 8, 10, 7));

        assertThat(ShopNoticeMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
