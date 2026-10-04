package com.tastyhouse.application.product.port.in;

public interface ProductManagementCreateUseCase {

    Long createProduct(ProductManagementCreateCommand command);
}
