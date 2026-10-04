package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductShopLinkResult;

public interface ProductShopLinkQueryUseCase {

    List<ProductShopLinkResult> getShopLinks(Long ceoId, Long shopId, Long productId);
}
