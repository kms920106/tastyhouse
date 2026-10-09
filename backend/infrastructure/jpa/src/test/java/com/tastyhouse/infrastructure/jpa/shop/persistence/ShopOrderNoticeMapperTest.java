package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopOrderNoticeMapperTest {

    @Test
    @DisplayName("ShopOrderNotice 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopOrderNotice() {
        ShopOrderNotice original = ShopOrderNotice.reconstitute(
            ShopOrderNoticeId.of(112L),
            ShopId.of(113L),
            "v14",
            true,
            "v16",
            LocalDateTime.of(2026, 1, 18, 10, 17),
            LocalDateTime.of(2026, 1, 19, 10, 18)
        );

        ShopOrderNoticeJpaEntity entity = ShopOrderNoticeMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(113L);
        assertThat(entity.getContent()).isEqualTo("v14");
        assertThat(entity.isHidden()).isEqualTo(true);
        assertThat(entity.getHiddenReason()).isEqualTo("v16");
    }

    @Test
    @DisplayName("ShopOrderNotice 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopOrderNotice() {
        ShopOrderNotice original = ShopOrderNotice.reconstitute(
            ShopOrderNoticeId.of(112L),
            ShopId.of(113L),
            "v14",
            true,
            "v16",
            LocalDateTime.of(2026, 1, 18, 10, 17),
            LocalDateTime.of(2026, 1, 19, 10, 18)
        );

        ShopOrderNoticeJpaEntity entity = ShopOrderNoticeJpaEntity.create(
            113L,
            "v14",
            true,
            "v16"
        );
        ReflectionTestUtils.setField(entity, "id", 112L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 18, 10, 17));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 19, 10, 18));

        assertThat(ShopOrderNoticeMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
