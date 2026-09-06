package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.repository.ProductShopLinkRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 ↔ 가게 연결 write 포트의 인메모리 fake. 순수 단위 테스트 전용이다.
 */
class FakeProductShopLinkRepository implements ProductShopLinkRepository {

    private final List<ProductShopLink> links = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public ProductShopLink save(ProductShopLink link) {
        if (link.getId() != null) {
            // 기존 링크는 같은 인스턴스를 들고 있으므로 재저장이 불필요하다(도메인이 직접 변경한다).
            return link;
        }
        ProductShopLink persisted = ProductShopLink.reconstitute(
            sequence.getAndIncrement(),
            link.getProductId(),
            link.getShopId(),
            link.getProductCategoryId(),
            link.getSort()
        );
        links.add(persisted);
        return persisted;
    }

    @Override
    public Optional<ProductShopLink> findByProductIdAndShopId(ProductId productId, ShopId shopId) {
        return links.stream()
            .filter(link -> link.getProductId().equals(productId) && link.getShopId().equals(shopId))
            .findFirst();
    }

    @Override
    public List<ProductShopLink> findAllByProductId(ProductId productId) {
        return links.stream()
            .filter(link -> link.getProductId().equals(productId))
            .toList();
    }

    @Override
    public List<ProductShopLink> findAllByShopId(ShopId shopId) {
        return links.stream()
            .filter(link -> link.getShopId().equals(shopId))
            .sorted(Comparator.comparing(ProductShopLink::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    @Override
    public boolean existsByProductIdAndShopId(ProductId productId, ShopId shopId) {
        return findByProductIdAndShopId(productId, shopId).isPresent();
    }

    @Override
    public long countByProductId(ProductId productId) {
        return findAllByProductId(productId).size();
    }

    @Override
    public void delete(ProductShopLink link) {
        links.removeIf(existing -> existing.getId() != null && existing.getId().equals(link.getId()));
    }

    /** 테스트 준비용 — 기존 연결을 미리 심는다. */
    void given(ProductId productId, ShopId shopId, ProductCategoryId categoryId, Integer sort) {
        save(ProductShopLink.of(productId, shopId, categoryId, sort));
    }
}
