package com.tastyhouse.domain.product.domain.model;

import lombok.Getter;

import com.tastyhouse.domain.product.domain.vo.ProductCategoryId;

/**
 * 상품 카테고리 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductCategoryJpaEntity} + {@code ProductCategoryMapper}가 담당한다.
 */
@Getter
public class ProductCategory {

    private final Long id;
    private final Long shopId;
    private String name;
    private Integer sort;
    private boolean visible;

    private ProductCategory(Long id, Long shopId, String name, Integer sort, boolean visible) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductCategory of(Long shopId, String name, Integer sort, boolean visible) {
        return new ProductCategory(null, shopId, name, sort, visible);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductCategory reconstitute(Long id, Long shopId, String name, Integer sort, boolean visible) {
        return new ProductCategory(id, shopId, name, sort, visible);
    }

    public ProductCategoryId getProductCategoryId() {
        return ProductCategoryId.of(this.id);
    }

    public void update(String displayName, Integer sort, boolean visible) {
        this.name = displayName;
        this.sort = sort;
        this.visible = visible;
    }
}
