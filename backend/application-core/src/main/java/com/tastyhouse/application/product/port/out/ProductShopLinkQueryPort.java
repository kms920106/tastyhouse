package com.tastyhouse.application.product.port.out;

import java.util.List;

public interface ProductShopLinkQueryPort {

    List<ProductShopLinkResult> findOwnedShopLinks(Long ceoId, Long productId);

    List<Long> findOwnedShopIds(Long ceoId);
}
