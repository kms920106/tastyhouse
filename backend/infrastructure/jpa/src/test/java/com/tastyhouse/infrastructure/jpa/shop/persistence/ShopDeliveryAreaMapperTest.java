package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDeliveryAreaMapperTest {

    @Test
    @DisplayName("ShopDeliveryArea 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopDeliveryArea() {
        ShopDeliveryArea original = ShopDeliveryArea.reconstitute(
            148L,
            ShopId.of(149L),
            AdminDongId.of(150L),
            DeliveryAreaSource.POLYGON
        );

        ShopDeliveryAreaJpaEntity entity = ShopDeliveryAreaMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(149L);
        assertThat(entity.getAdminDongId()).isEqualTo(150L);
        assertThat(entity.getSource()).isEqualTo("POLYGON");
    }

    @Test
    @DisplayName("ShopDeliveryArea 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopDeliveryArea() {
        ShopDeliveryArea original = ShopDeliveryArea.reconstitute(
            148L,
            ShopId.of(149L),
            AdminDongId.of(150L),
            DeliveryAreaSource.POLYGON
        );

        ShopDeliveryAreaJpaEntity entity = ShopDeliveryAreaJpaEntity.create(
            149L,
            150L,
            "POLYGON"
        );
        ReflectionTestUtils.setField(entity, "id", 148L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopDeliveryAreaMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
