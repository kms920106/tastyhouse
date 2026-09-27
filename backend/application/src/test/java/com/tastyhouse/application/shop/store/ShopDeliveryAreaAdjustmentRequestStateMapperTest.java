package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDeliveryAreaAdjustmentRequestStateMapperTest {

    @Test
    @DisplayName("ShopDeliveryAreaAdjustmentRequest → ShopDeliveryAreaAdjustmentRequestState → ShopDeliveryAreaAdjustmentRequest 왕복 시 모든 필드가 보존된다")
    void shopDeliveryAreaAdjustmentRequestRoundTrip() {
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

        ShopDeliveryAreaAdjustmentRequest restored = ShopDeliveryAreaAdjustmentRequestStateMapper.toDomain(ShopDeliveryAreaAdjustmentRequestStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
