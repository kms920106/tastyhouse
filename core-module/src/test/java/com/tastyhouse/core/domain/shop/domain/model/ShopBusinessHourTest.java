package com.tastyhouse.core.domain.shop.domain.model;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopBusinessHourTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBusinessHour() {
        ShopBusinessHour businessHour = ShopBusinessHour.of(1L, DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false);

        assertThat(businessHour.getId()).isNull();
        assertThat(businessHour.getShopId()).isEqualTo(1L);
        assertThat(businessHour.getDayType()).isEqualTo(DayType.WEEKDAY);
        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(businessHour.getCloseTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(businessHour.getIsClosed()).isFalse();
    }

    @Test
    @DisplayName("update는 영업시간 정보를 변경한다")
    void update_changesFields() {
        ShopBusinessHour businessHour = ShopBusinessHour.of(1L, DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false);

        businessHour.update(DayType.SUNDAY, LocalTime.of(10, 0), LocalTime.of(20, 0), true);

        assertThat(businessHour.getDayType()).isEqualTo(DayType.SUNDAY);
        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(businessHour.getCloseTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(businessHour.getIsClosed()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopBusinessHour businessHour = ShopBusinessHour.reconstitute(
            1L, 2L, DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false
        );

        assertThat(businessHour.getId()).isEqualTo(1L);
        assertThat(businessHour.getShopId()).isEqualTo(2L);
    }
}
