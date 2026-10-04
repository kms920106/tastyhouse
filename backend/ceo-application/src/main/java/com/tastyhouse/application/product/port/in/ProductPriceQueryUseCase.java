package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOwnerPriceView;

public interface ProductPriceQueryUseCase {

    List<ProductOwnerPriceView> getPrices(Long ceoId, Long shopId, Long productId);
}
