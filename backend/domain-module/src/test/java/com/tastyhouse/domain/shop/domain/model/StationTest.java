package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.shop.model.Station;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StationTest {

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·역명을 재구성한다")
    void reconstitute_restoresPersistedState() {
        Station station = Station.reconstitute(1L, "강남역");

        assertThat(station.getId()).isEqualTo(1L);
        assertThat(station.getStationName()).isEqualTo("강남역");
    }
}
