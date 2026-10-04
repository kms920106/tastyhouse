package com.tastyhouse.application.product.service;

import com.tastyhouse.domain.exception.ErrorCodeSpec;

public record ProductAvailabilityFailure(
    Long id,
    String name,
    ErrorCodeSpec errorCode
) {

    public static ProductAvailabilityFailure of(Long id, String name, ErrorCodeSpec errorCode) {
        return new ProductAvailabilityFailure(id, name, errorCode);
    }
}
