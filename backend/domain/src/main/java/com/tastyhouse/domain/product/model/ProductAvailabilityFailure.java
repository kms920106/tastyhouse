package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.ErrorCode;

public record ProductAvailabilityFailure(
    Long id,
    String name,
    ErrorCode errorCode
) {

    public static ProductAvailabilityFailure of(Long id, String name, ErrorCode errorCode) {
        return new ProductAvailabilityFailure(id, name, errorCode);
    }
}
