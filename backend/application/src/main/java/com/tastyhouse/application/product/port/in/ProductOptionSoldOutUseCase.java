package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

@CeoApp
public interface ProductOptionSoldOutUseCase {

    ProductAvailabilityChangeView markOptionsSoldOut(ProductOptionSoldOutCommand command);
}
