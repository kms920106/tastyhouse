package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductVegetarianStatusResult;

public interface ProductVegetarianQueryUseCase {

    ProductVegetarianStatusResult getVegetarianStatus(Long ceoId, Long shopId, Long productId);
}
