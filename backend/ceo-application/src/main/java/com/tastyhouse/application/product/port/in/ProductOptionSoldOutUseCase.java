package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

public interface ProductOptionSoldOutUseCase {

    ProductAvailabilityChangeView markOptionsSoldOut(ProductOptionSoldOutCommand command);
}
