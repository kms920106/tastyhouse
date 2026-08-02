package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.shop.model.ShopAmenity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

class ShopAmenityTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientShopAmenity() {
        ShopAmenity amenity = ShopAmenity.of(ShopId.of(1L), ShopAmenityCategoryId.of(2L));

        assertThat(amenity.getId()).isNull();
        assertThat(amenity.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(amenity.getShopAmenityCategoryId()).isEqualTo(ShopAmenityCategoryId.of(2L));
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopAmenity amenity = ShopAmenity.reconstitute(1L, ShopId.of(2L), ShopAmenityCategoryId.of(3L));

        assertThat(amenity.getId()).isEqualTo(1L);
        assertThat(amenity.getShopId()).isEqualTo(ShopId.of(2L));
        assertThat(amenity.getShopAmenityCategoryId()).isEqualTo(ShopAmenityCategoryId.of(3L));
    }
}
