package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * Product와 BBQ 메뉴 ID 매핑 순수 도메인 모델 — BBQ API 외부 메뉴 ID 임시 저장.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductBbqJpaEntity} + {@code ProductBbqMapper}가 담당한다.
 */
public class ProductBbq {

    private final Long id;
    private final ProductId productId;
    private final BbqMenuId bbqMenuId;
    private final BbqCategoryId bbqCategoryId;
    private boolean optionsSynced;

    private ProductBbq(Long id, ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        this.id = id;
        this.productId = productId;
        this.bbqMenuId = bbqMenuId;
        this.bbqCategoryId = bbqCategoryId;
        this.optionsSynced = optionsSynced;
    }

    public static ProductBbq of(ProductId productId, BbqMenuId bbqMenuId, BbqCategoryId bbqCategoryId, boolean optionsSynced) {
        return new ProductBbq(null, productId, bbqMenuId, bbqCategoryId, optionsSynced);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductBbq reconstitute(
        Long id,
        ProductId productId,
        BbqMenuId bbqMenuId,
        BbqCategoryId bbqCategoryId,
        boolean optionsSynced
    ) {
        return new ProductBbq(id, productId, bbqMenuId, bbqCategoryId, optionsSynced);
    }

    public void markOptionsSynced() {
        this.optionsSynced = true;
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public BbqMenuId getBbqMenuId() {
        return this.bbqMenuId;
    }

    public BbqCategoryId getBbqCategoryId() {
        return this.bbqCategoryId;
    }

    public boolean isOptionsSynced() {
        return this.optionsSynced;
    }
}
