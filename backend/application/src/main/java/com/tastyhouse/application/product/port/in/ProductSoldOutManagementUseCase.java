package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ProductSoldOutManagementUseCase {

    void markSoldOut(ProductSoldOutManagementCommand command);
}
