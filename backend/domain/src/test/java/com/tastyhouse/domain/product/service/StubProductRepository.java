package com.tastyhouse.domain.product.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class StubProductRepository implements ProductRepository {
    private final Map<Long, Product> products;

    StubProductRepository(Map<Long, Product> products) {
        this.products = products;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return Optional.ofNullable(products.get(id.value()));
    }

    @Override
    public Optional<Product> findByIdIncludingDeleted(ProductId id) {
        return findById(id);
    }

    @Override
    public Product save(Product product) {
        return product;
    }

    @Override
    public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
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
    public long countRepresentativeByShopId(ShopId shopId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Product> findAllSoldOutExpiredBefore(java.time.LocalDateTime baseTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsByShopIdAndName(ShopId shopId, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Product> findAllByShopIdAndCategoryId(
        ShopId shopId,
        ProductCategoryId productCategoryId
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countByCategoryId(ProductCategoryId productCategoryId) {
        throw new UnsupportedOperationException();
    }
}
