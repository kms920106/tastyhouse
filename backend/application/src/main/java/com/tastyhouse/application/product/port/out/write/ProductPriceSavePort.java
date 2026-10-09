package com.tastyhouse.application.product.port.out.write;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductPriceId;

public interface ProductPriceSavePort {

    ProductPrice save(ProductPrice productPrice);

    void deleteAllByIdIn(List<ProductPriceId> ids);
}
