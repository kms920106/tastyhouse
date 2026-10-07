package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;

public interface ProductOptionAvailabilityListQueryUseCase {

    List<ProductOptionAvailabilityGroupResult> getProductOptionAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    );
}
