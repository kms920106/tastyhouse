package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductShopLinkResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductShopLinkQueryUseCase {

    List<ProductShopLinkResult> getShopLinks(Long ceoId, Long shopId, Long productId);
}
