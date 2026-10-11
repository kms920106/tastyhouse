package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductAvailabilityChangeView(
    List<Long> succeeded,
    List<Failure> failed
) {

    public record Failure(
        Long id,
        String name,
        String code,
        String message
    ) {
    }
}
