package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOptionGroupCommandUseCase {

    Long createProductOptionGroup(ProductOptionGroupOwnerCreateCommand command);

    void updateProductOptionGroup(ProductOptionGroupUpdateCommand command);

    void deleteProductOptionGroup(ProductOptionGroupDeleteCommand command);
}
