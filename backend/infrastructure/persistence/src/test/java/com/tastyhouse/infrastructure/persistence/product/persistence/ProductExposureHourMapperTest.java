package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

import static org.assertj.core.api.Assertions.assertThat;

class ProductExposureHourMapperTest {

    @Test
    @DisplayName("ProductExposureHour → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductExposureHour productExposureHour = ProductExposureHour.reconstitute(
            31L,
            ProductId.of(32L),
            DayType.WEEKEND,
            LocalTime.of(9, 30),
            LocalTime.of(21, 45)
        );

        ProductExposureHourJpaEntity entity = ProductExposureHourMapper.toEntity(productExposureHour);

        assertThat(entity.getProductId()).isEqualTo(32L);
        assertThat(entity.getDayType()).isEqualTo("WEEKEND");
        assertThat(entity.getStartTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(entity.getEndTime()).isEqualTo(LocalTime.of(21, 45));
    }

    @Test
    @DisplayName("엔티티 → ProductExposureHour 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductExposureHourJpaEntity entity = ProductExposureHourJpaEntity.create(
            32L,
            "WEEKEND",
            LocalTime.of(9, 30),
            LocalTime.of(21, 45)
        );
        ReflectionTestUtils.setField(entity, "id", 31L);

        ProductExposureHour restored = ProductExposureHourMapper.toDomain(entity);

        ProductExposureHour expected = ProductExposureHour.reconstitute(
            31L,
            ProductId.of(32L),
            DayType.WEEKEND,
            LocalTime.of(9, 30),
            LocalTime.of(21, 45)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
