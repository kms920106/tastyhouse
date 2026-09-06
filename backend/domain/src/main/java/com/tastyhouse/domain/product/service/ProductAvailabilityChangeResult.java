package com.tastyhouse.domain.product.service;

import java.util.List;

public record ProductAvailabilityChangeResult(
    List<Long> succeeded,
    List<ProductAvailabilityFailure> failed
) {
    public ProductAvailabilityChangeResult {
        succeeded = succeeded != null ? List.copyOf(succeeded) : List.of();
        failed = failed != null ? List.copyOf(failed) : List.of();
    }

    public static ProductAvailabilityChangeResult of(
        List<Long> succeeded,
        List<ProductAvailabilityFailure> failed
    ) {
        return new ProductAvailabilityChangeResult(
            succeeded,
            failed
        );
    }
}
