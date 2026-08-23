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

/**
 * 메뉴-가게 연결 테스트 전용 {@code ProductRepository} 스텁.
 *
 * <p>{@code StubProductRepository}를 쓰지 않는 이유는 그쪽이 {@code countVisibleByShopId}에
 * {@code UnsupportedOperationException}을 던지는데, 연결 해제 판정("그 가게 메뉴판에 노출 메뉴가
 * 남는가")이 바로 그 조회를 쓰기 때문이다.
 *
 * <p>호출되지 않는 메서드는 그대로 {@code UnsupportedOperationException}을 던진다 — 조용히 빈 값을
 * 돌려주면 테스트가 잘못된 전제 위에서 통과할 수 있다.
 */
class FakeShopLinkProductRepository implements ProductRepository {

    private final Map<Long, Product> products = new HashMap<>();
    private final Map<Long, Long> visibleCountByShopId = new HashMap<>();

    void given(Product product) {
        products.put(product.getId(), product);
    }

    /** 그 가게 메뉴판의 현재 노출 메뉴 수를 지정한다(연결 해제 판정의 입력). */
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
