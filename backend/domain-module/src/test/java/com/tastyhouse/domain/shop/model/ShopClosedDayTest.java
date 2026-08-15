package com.tastyhouse.domain.shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shop.vo.ShopId;

class ShopClosedDayTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientClosedDay() {
        ShopClosedDay closedDay = ShopClosedDay.of(ShopId.of(1L), ClosedDayType.EVERY_WEEK_MONDAY);

        assertThat(closedDay.getId()).isNull();
        assertThat(closedDay.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(closedDay.getClosedDayType()).isEqualTo(ClosedDayType.EVERY_WEEK_MONDAY);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopClosedDay closedDay = ShopClosedDay.reconstitute(1L, ShopId.of(2L), ClosedDayType.EVERY_WEEK_MONDAY);

        assertThat(closedDay.getId()).isEqualTo(1L);
        assertThat(closedDay.getShopId()).isEqualTo(ShopId.of(2L));
    }
}
