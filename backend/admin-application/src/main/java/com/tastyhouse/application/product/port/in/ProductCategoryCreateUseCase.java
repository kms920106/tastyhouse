package com.tastyhouse.application.product.port.in;

public interface ProductCategoryCreateUseCase {

    Long createProductCategory(ProductCategoryManagementCreateCommand command);
}
