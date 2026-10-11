package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductShopLink;

public interface ProductShopLinkSavePort {

    ProductShopLink save(ProductShopLink link);

    void delete(ProductShopLink link);
}
