package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDeliveryAreaAdjustmentRequestMapperTest {

    @Test
    @DisplayName("ShopDeliveryAreaAdjustmentRequest 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopDeliveryAreaAdjustmentRequest() {
        ShopDeliveryAreaAdjustmentRequest original = ShopDeliveryAreaAdjustmentRequest.reconstitute(
            152L,
            ShopId.of(153L),
            "v54",
            "v55",
            "v56",
            "v57",
            UploadedFileId.of(158L),
            DeliveryAreaAdjustmentStatus.CANCELED,
            "v60",
            LocalDateTime.of(2026, 1, 6, 10, 1),
            LocalDateTime.of(2026, 1, 7, 10, 2)
        );

        ShopDeliveryAreaAdjustmentRequestJpaEntity entity = ShopDeliveryAreaAdjustmentRequestMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(153L);
        assertThat(entity.getCounterpartShopName()).isEqualTo("v54");
        assertThat(entity.getCounterpartBusinessNumber()).isEqualTo("v55");
        assertThat(entity.getFranchiseName()).isEqualTo("v56");
        assertThat(entity.getReason()).isEqualTo("v57");
        assertThat(entity.getConsentFileId()).isEqualTo(158L);
        assertThat(entity.getStatus()).isEqualTo("CANCELED");
        assertThat(entity.getRejectReason()).isEqualTo("v60");
    }

    @Test
    @DisplayName("ShopDeliveryAreaAdjustmentRequest 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopDeliveryAreaAdjustmentRequest() {
        ShopDeliveryAreaAdjustmentRequest original = ShopDeliveryAreaAdjustmentRequest.reconstitute(
            152L,
            ShopId.of(153L),
            "v54",
            "v55",
            "v56",
            "v57",
            UploadedFileId.of(158L),
            DeliveryAreaAdjustmentStatus.CANCELED,
            "v60",
            LocalDateTime.of(2026, 1, 6, 10, 1),
            LocalDateTime.of(2026, 1, 7, 10, 2)
        );

        ShopDeliveryAreaAdjustmentRequestJpaEntity entity = ShopDeliveryAreaAdjustmentRequestJpaEntity.create(
            153L,
            "v54",
            "v55",
            "v56",
            "v57",
            158L,
            "CANCELED",
            "v60"
        );
        ReflectionTestUtils.setField(entity, "id", 152L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 6, 10, 1));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 7, 10, 2));

        assertThat(ShopDeliveryAreaAdjustmentRequestMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
