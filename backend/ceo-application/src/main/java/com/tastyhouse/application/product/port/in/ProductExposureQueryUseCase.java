package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductExposureViewResult;

public interface ProductExposureQueryUseCase {

    ProductExposureViewResult getExposure(Long ceoId, Long shopId, Long productId);
}
