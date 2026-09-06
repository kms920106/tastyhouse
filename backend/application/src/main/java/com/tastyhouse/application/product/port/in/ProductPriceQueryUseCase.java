package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOwnerPriceView;

@CeoApp
public interface ProductPriceQueryUseCase {

    List<ProductOwnerPriceView> getPrices(Long ceoId, Long shopId, Long productId);
}
