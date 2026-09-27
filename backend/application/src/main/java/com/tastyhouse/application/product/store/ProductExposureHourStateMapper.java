package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductExposureHourState;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

final class ProductExposureHourStateMapper {
    private ProductExposureHourStateMapper() {
    }

    static ProductExposureHour toDomain(ProductExposureHourState state) {
        return ProductExposureHour.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.dayType() == null ? null : DayType.valueOf(state.dayType()),
            state.startTime(),
            state.endTime()
        );
    }

    static ProductExposureHourState toState(ProductExposureHour hour) {
        return new ProductExposureHourState(
            hour.getId(),
            hour.getProductId() == null ? null : hour.getProductId().value(),
            hour.getDayType() == null ? null : hour.getDayType().name(),
            hour.getStartTime(),
            hour.getEndTime()
        );
    }
}
