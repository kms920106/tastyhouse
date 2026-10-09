package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductLoadPort {

    Optional<Product> findActiveById(ProductId id);

    List<Product> findAllActiveByShopIdAndIdIn(ShopId shopId, List<ProductId> ids);

    long countVisibleByShopId(ShopId shopId);

    long countVisibleRepresentativeByShopId(ShopId shopId);

    long countActiveRepresentativeByShopId(ShopId shopId);

    List<Product> findAllActiveSoldOutExpiredBefore(LocalDateTime baseTime);

    boolean existsActiveByShopIdAndName(ShopId shopId, String name);

    boolean existsActiveByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId);

    List<Product> findAllActiveByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId);

    long countActiveByCategoryId(ProductCategoryId productCategoryId);

    Optional<Product> findByIdIncludingDeleted(ProductId id);
}
