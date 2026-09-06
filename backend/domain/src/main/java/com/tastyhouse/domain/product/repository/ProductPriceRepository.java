package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductPriceRepository {
    ProductPrice save(ProductPrice productPrice);

    Optional<ProductPrice> findById(ProductPriceId id);

    List<ProductPrice> findAllByProductId(ProductId productId);

    List<ProductPrice> findAllByShopId(ShopId shopId);

    void deleteAllByIdIn(List<ProductPriceId> ids);
}
