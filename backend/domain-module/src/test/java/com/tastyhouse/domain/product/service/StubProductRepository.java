package com.tastyhouse.domain.product.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * {@code findById}만 실제로 동작하는 최소 스텁 — product 도메인의 순수 단위 테스트가 공유한다.
 *
 * <p>나머지 메서드는 이 스텁을 쓰는 테스트가 호출하지 않으므로 {@code UnsupportedOperationException}을
 * 던진다 — 조용히 빈 값을 돌려주면 테스트가 잘못된 전제 위에서 통과할 수 있다.
 */
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
