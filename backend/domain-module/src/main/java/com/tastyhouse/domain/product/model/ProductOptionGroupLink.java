package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 메뉴 ↔ 일반 옵션그룹 연결(N:M) 순수 도메인 모델.
 *
 * <p><b>{@code sort}가 그룹이 아니라 이 연결에 있는 것이 핵심이다</b> — "옵션그룹 순서는 메뉴별"이므로
 * 정렬은 그룹의 속성이 아니라 연결의 속성이다. 같은 그룹이 메뉴 A에서는 1번, 메뉴 B에서는 3번일 수 있다.
 *
 * <p>영속화는 infrastructure-module의 {@code ProductOptionGroupLinkJpaEntity} +
 * {@code ProductOptionGroupLinkMapper}가 담당한다.
 */
public class ProductOptionGroupLink {

    private final Long id;
    private final ProductId productId;
    private final ProductOptionGroupId optionGroupId;
    private Integer sort;

    private ProductOptionGroupLink(
        Long id,
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        this.id = id;
        this.productId = productId;
        this.optionGroupId = optionGroupId;
        this.sort = sort;
    }

    public static ProductOptionGroupLink of(
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        return new ProductOptionGroupLink(null, productId, optionGroupId, sort);
    }

    public static ProductOptionGroupLink reconstitute(
        Long id,
        ProductId productId,
        ProductOptionGroupId optionGroupId,
        Integer sort
    ) {
        return new ProductOptionGroupLink(id, productId, optionGroupId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ProductOptionGroupId getOptionGroupId() {
        return this.optionGroupId;
    }

    public Integer getSort() {
        return this.sort;
    }

    /** 이 메뉴에서의 옵션그룹 정렬 순서를 바꾼다. */
    public void changeSort(Integer sort) {
        this.sort = sort;
    }
}
