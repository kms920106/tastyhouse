package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductAvailabilityGroupResult;

public interface ProductAvailabilityListQueryUseCase {

    List<ProductAvailabilityGroupResult> getProductAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    );
}
