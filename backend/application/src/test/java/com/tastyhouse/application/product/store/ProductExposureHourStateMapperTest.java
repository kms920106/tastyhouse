package com.tastyhouse.application.product.store;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

import static org.assertj.core.api.Assertions.assertThat;

class ProductExposureHourStateMapperTest {

    @Test
    @DisplayName("ProductExposureHour → ProductExposureHourState → ProductExposureHour 왕복 시 모든 필드가 보존된다")
    void productExposureHourRoundTrip() {
        ProductExposureHour original = ProductExposureHour.reconstitute(
            31L,
            ProductId.of(32L),
            DayType.WEEKEND,
            LocalTime.of(9, 30),
            LocalTime.of(21, 45)
        );

        ProductExposureHour restored = ProductExposureHourStateMapper.toDomain(ProductExposureHourStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
