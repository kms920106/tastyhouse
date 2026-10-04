package com.tastyhouse.application.product.port.in;

public interface ProductSoldOutManagementUseCase {

    void markSoldOut(ProductSoldOutManagementCommand command);
}
