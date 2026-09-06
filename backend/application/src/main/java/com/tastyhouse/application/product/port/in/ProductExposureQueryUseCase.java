package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductExposureViewResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductExposureQueryUseCase {

    ProductExposureViewResult getExposure(Long ceoId, Long shopId, Long productId);
}
