package com.tastyhouse.domain.product.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴-가게 연결 테스트 전용 {@code ProductCategoryRepository} 스텁.
 * 메뉴그룹의 소속 가게 대조({@code PRODUCT_SHOP_LINK_CATEGORY_MISMATCH})만 검증하면 되므로
 * {@code findById}만 실제로 동작한다.
 */
class FakeShopLinkProductCategoryRepository implements ProductCategoryRepository {

    private final Map<Long, ProductCategory> categories = new HashMap<>();

    /** 테스트 준비용 — 어느 가게에 속한 메뉴그룹인지 심는다. */
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
