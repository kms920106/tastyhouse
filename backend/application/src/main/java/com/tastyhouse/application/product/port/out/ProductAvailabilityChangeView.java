package com.tastyhouse.application.product.port.out;

import java.util.List;

import com.tastyhouse.domain.exception.ErrorCode;

public record ProductAvailabilityChangeView(
    List<Long> succeeded,
    List<Failure> failed
) {

    public record Failure(
        Long id,
        String name,
        ErrorCode errorCode
    ) {
    }
}
