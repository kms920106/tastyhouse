package com.tastyhouse.domain.product.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

class FakeShopLinkProductCategoryRepository implements ProductCategoryRepository {
    private final Map<Long, ProductCategory> categories = new HashMap<>();

    void given(Long categoryId, ShopId shopId) {
        categories.put(categoryId, ProductCategory.reconstitute(categoryId, shopId, "그룹" + categoryId, null, 0, true));
    }

    @Override
    public Optional<ProductCategory> findById(ProductCategoryId id) {
        return Optional.ofNullable(categories.get(id.value()));
    }

    @Override
    public List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProductCategory save(ProductCategory productCategory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ProductCategory> findAllByShopId(ShopId shopId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(ProductCategory productCategory) {
        throw new UnsupportedOperationException();
    }
}
