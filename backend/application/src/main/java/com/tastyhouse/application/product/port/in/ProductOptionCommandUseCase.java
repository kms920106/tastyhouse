package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOptionCommandUseCase {

    Long createProductOption(ProductOptionOwnerCreateCommand command);

    void updateProductOption(ProductOptionUpdateCommand command);

    void deleteProductOption(ProductOptionDeleteCommand command);

    void changeProductOptionOrder(ProductOptionOrderChangeCommand command);
}
