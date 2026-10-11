package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductPriceLoadPort {

    Optional<ProductPrice> findById(ProductPriceId id);

    List<ProductPrice> findAllByProductId(ProductId productId);

    List<ProductPrice> findAllOfActiveProductsByShopId(ShopId shopId);
}
