package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.product.port.out.ProductAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;

@CeoApp
public interface ProductAvailabilityQueryUseCase {

    List<ProductAvailabilityGroupResult> getProductAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    );

    List<ProductOptionAvailabilityGroupResult> getProductOptionAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    );
}
