package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopBreakTimeTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBreakTime() {
        ShopBreakTime breakTime = ShopBreakTime.of(1L, DayType.WEEKDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

        assertThat(breakTime.getId()).isNull();
        assertThat(breakTime.getShopId()).isEqualTo(1L);
        assertThat(breakTime.getDayType()).isEqualTo(DayType.WEEKDAY);
        assertThat(breakTime.getStartTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(breakTime.getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("update는 브레이크타임 정보를 변경한다")
    void update_changesFields() {
        ShopBreakTime breakTime = ShopBreakTime.of(1L, DayType.WEEKDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

        breakTime.update(DayType.SATURDAY, LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertThat(breakTime.getDayType()).isEqualTo(DayType.SATURDAY);
        assertThat(breakTime.getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(breakTime.getEndTime()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopBreakTime breakTime = ShopBreakTime.reconstitute(1L, 2L, DayType.WEEKDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

        assertThat(breakTime.getId()).isEqualTo(1L);
        assertThat(breakTime.getShopId()).isEqualTo(2L);
    }
}
