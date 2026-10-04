package com.tastyhouse.application.product.port.in;

public interface ProductCategoryCommandUseCase {

    Long createProductCategory(ProductCategoryOwnerCreateCommand command);

    void updateProductCategory(ProductCategoryUpdateCommand command);

    void deleteProductCategory(ProductCategoryDeleteCommand command);
}
