package com.tastyhouse.application.shop.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDeliveryAreaStateMapperTest {

    @Test
    @DisplayName("ShopDeliveryArea → ShopDeliveryAreaState → ShopDeliveryArea 왕복 시 모든 필드가 보존된다")
    void shopDeliveryAreaRoundTrip() {
        ShopDeliveryArea original = ShopDeliveryArea.reconstitute(
            148L,
            ShopId.of(149L),
            AdminDongId.of(150L),
            DeliveryAreaSource.POLYGON
        );

        ShopDeliveryArea restored = ShopDeliveryAreaStateMapper.toDomain(ShopDeliveryAreaStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
