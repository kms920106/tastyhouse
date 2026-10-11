package com.tastyhouse.testsupport.product.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;

public final class StubProductPersistence implements ProductLoadPort, ProductSavePort {

    private final Map<Long, Product> products;

    public StubProductPersistence(Map<Long, Product> products) {
        this.products = products;
    }

    @Override
    public Optional<Product> findActiveById(ProductId id) {
        return Optional.ofNullable(products.get(id.value()));
    }

    @Override
    public Optional<Product> findByIdIncludingDeleted(ProductId id) {
        return findActiveById(id);
    }

    @Override
    public Product save(Product product) {
        return product;
    }

    @Override
    public List<Product> findAllActiveByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countVisibleByShopId(ShopId shopId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countVisibleRepresentativeByShopId(ShopId shopId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countActiveRepresentativeByShopId(ShopId shopId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Product> findAllActiveSoldOutExpiredBefore(java.time.LocalDateTime baseTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsActiveByShopIdAndName(ShopId shopId, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsActiveByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Product> findAllActiveByShopIdAndCategoryId(
        ShopId shopId,
        ProductCategoryId productCategoryId
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countActiveByCategoryId(ProductCategoryId productCategoryId) {
        throw new UnsupportedOperationException();
    }
}
