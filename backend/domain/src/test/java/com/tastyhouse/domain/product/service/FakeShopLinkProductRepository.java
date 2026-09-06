package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

class FakeShopLinkProductRepository implements ProductRepository {
    private final Map<Long, Product> products = new HashMap<>();
    private final Map<Long, Long> visibleCountByShopId = new HashMap<>();

    void given(Product product) {
        products.put(product.getId(), product);
    }

    void givenVisibleCount(ShopId shopId, long count) {
        visibleCountByShopId.put(shopId.value(), count);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return Optional.ofNullable(products.get(id.value()));
    }

    @Override
    public Product save(Product product) {
        return product;
    }

    @Override
    public long countVisibleByShopId(ShopId shopId) {
        return visibleCountByShopId.getOrDefault(shopId.value(), 0L);
    }

    @Override
    public Optional<Product> findByIdIncludingDeleted(ProductId id) {
        return findById(id);
    }

    @Override
    public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
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
    public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
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
    public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countByCategoryId(ProductCategoryId productCategoryId) {
        throw new UnsupportedOperationException();
    }
}
