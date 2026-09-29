package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductPersistencePort {
    Optional<Product> findById(ProductId id);

    Product save(Product product);

    List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids);

    long countVisibleByShopId(ShopId shopId);

    long countVisibleRepresentativeByShopId(ShopId shopId);

    long countRepresentativeByShopId(ShopId shopId);

    List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime);

    boolean existsByShopIdAndName(ShopId shopId, String name);

    boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId);

    List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId);

    long countByCategoryId(ProductCategoryId productCategoryId);

    Optional<Product> findByIdIncludingDeleted(ProductId id);
}
