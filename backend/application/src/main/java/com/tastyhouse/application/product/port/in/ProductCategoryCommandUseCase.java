package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductCategoryCommandUseCase {

    Long createProductCategory(ProductCategoryOwnerCreateCommand command);

    void updateProductCategory(ProductCategoryUpdateCommand command);

    void deleteProductCategory(ProductCategoryDeleteCommand command);
}
