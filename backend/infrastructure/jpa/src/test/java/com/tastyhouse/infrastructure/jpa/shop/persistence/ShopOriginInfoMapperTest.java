package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopOriginInfoMapperTest {

    @Test
    @DisplayName("ShopOriginInfo 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopOriginInfo() {
        ShopOriginInfo original = ShopOriginInfo.reconstitute(
            154L,
            ShopId.of(155L),
            OriginSourceType.DIRECT,
            "v57",
            "v58",
            LocalDateTime.of(2026, 1, 4, 10, 59),
            LocalDateTime.of(2026, 1, 5, 10, 0)
        );

        ShopOriginInfoJpaEntity entity = ShopOriginInfoMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(155L);
        assertThat(entity.getSourceType()).isEqualTo("DIRECT");
        assertThat(entity.getContent()).isEqualTo("v57");
        assertThat(entity.getUrl()).isEqualTo("v58");
    }

    @Test
    @DisplayName("ShopOriginInfo 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopOriginInfo() {
        ShopOriginInfo original = ShopOriginInfo.reconstitute(
            154L,
            ShopId.of(155L),
            OriginSourceType.DIRECT,
            "v57",
            "v58",
            LocalDateTime.of(2026, 1, 4, 10, 59),
            LocalDateTime.of(2026, 1, 5, 10, 0)
        );

        ShopOriginInfoJpaEntity entity = ShopOriginInfoJpaEntity.create(
            155L,
            "DIRECT",
            "v57",
            "v58"
        );
        ReflectionTestUtils.setField(entity, "id", 154L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 4, 10, 59));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 5, 10, 0));

        assertThat(ShopOriginInfoMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
