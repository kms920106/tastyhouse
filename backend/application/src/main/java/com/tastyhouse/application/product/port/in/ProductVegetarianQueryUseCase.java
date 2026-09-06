package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductVegetarianStatusResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductVegetarianQueryUseCase {

    ProductVegetarianStatusResult getVegetarianStatus(Long ceoId, Long shopId, Long productId);
}
