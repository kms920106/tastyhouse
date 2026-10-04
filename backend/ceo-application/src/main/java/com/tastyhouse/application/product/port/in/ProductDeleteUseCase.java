package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

public interface ProductDeleteUseCase {

    ProductAvailabilityChangeView deleteProducts(ProductDeleteCommand command);
}
