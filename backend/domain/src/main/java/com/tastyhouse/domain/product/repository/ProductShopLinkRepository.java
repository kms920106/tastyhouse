package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductShopLinkRepository {
    ProductShopLink save(ProductShopLink link);

    Optional<ProductShopLink> findByProductIdAndShopId(ProductId productId, ShopId shopId);

    List<ProductShopLink> findAllByProductId(ProductId productId);

    List<ProductShopLink> findAllByShopId(ShopId shopId);

    boolean existsByProductIdAndShopId(ProductId productId, ShopId shopId);

    long countByProductId(ProductId productId);

    void delete(ProductShopLink link);
}
