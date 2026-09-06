package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOwnerCreateUseCase {

    Long createProduct(ProductOwnerCreateCommand command);
}
