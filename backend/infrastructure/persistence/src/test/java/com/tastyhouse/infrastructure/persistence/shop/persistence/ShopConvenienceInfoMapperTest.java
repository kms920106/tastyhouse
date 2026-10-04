package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopConvenienceInfoMapperTest {

    @Test
    @DisplayName("ShopConvenienceInfo 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopConvenienceInfo() {
        ShopConvenienceInfo original = ShopConvenienceInfo.reconstitute(
            137L,
            ShopId.of(138L),
            true,
            false,
            true,
            false,
            "v43",
            new BigDecimal("44.125"),
            new BigDecimal("45.125"),
            LocalDateTime.of(2026, 1, 19, 10, 46),
            LocalDateTime.of(2026, 1, 20, 10, 47)
        );

        ShopConvenienceInfoJpaEntity entity = ShopConvenienceInfoMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(138L);
        assertThat(entity.isParkingAvailable()).isEqualTo(true);
        assertThat(entity.isParkingPaid()).isEqualTo(false);
        assertThat(entity.isValetAvailable()).isEqualTo(true);
        assertThat(entity.isValetPaid()).isEqualTo(false);
        assertThat(entity.getDirectionsGuide()).isEqualTo("v43");
        assertThat(entity.getDisplayLatitude()).isEqualTo(new BigDecimal("44.125"));
        assertThat(entity.getDisplayLongitude()).isEqualTo(new BigDecimal("45.125"));
    }

    @Test
    @DisplayName("ShopConvenienceInfo 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopConvenienceInfo() {
        ShopConvenienceInfo original = ShopConvenienceInfo.reconstitute(
            137L,
            ShopId.of(138L),
            true,
            false,
            true,
            false,
            "v43",
            new BigDecimal("44.125"),
            new BigDecimal("45.125"),
            LocalDateTime.of(2026, 1, 19, 10, 46),
            LocalDateTime.of(2026, 1, 20, 10, 47)
        );

        ShopConvenienceInfoJpaEntity entity = ShopConvenienceInfoJpaEntity.create(
            138L,
            true,
            false,
            true,
            false,
            "v43",
            new BigDecimal("44.125"),
            new BigDecimal("45.125")
        );
        ReflectionTestUtils.setField(entity, "id", 137L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 19, 10, 46));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 20, 10, 47));

        assertThat(ShopConvenienceInfoMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
