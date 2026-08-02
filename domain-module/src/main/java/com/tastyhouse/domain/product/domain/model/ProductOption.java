package com.tastyhouse.domain.product.domain.model;

import lombok.Getter;

import com.tastyhouse.domain.product.domain.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.domain.vo.ProductOptionId;

/**
 * 상품 옵션 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductOptionJpaEntity} + {@code ProductOptionMapper}가 담당한다.
 */
@Getter
public class ProductOption {

    private final Long id;
    private final ProductOptionGroupId optionGroupId;
    private String name;
    private Integer additionalPrice;
    private Integer sort;
    private boolean soldOut;
    private boolean visible;

    private ProductOption(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        this.id = id;
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.visible = visible;
    }

    public static ProductOption of(
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        return new ProductOption(
            null,
            optionGroupId,
            name,
            additionalPrice != null ? additionalPrice : 0,
            sort,
            soldOut,
            visible
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductOption reconstitute(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        return new ProductOption(id, optionGroupId, name, additionalPrice, sort, soldOut, visible);
    }

    public ProductOptionId getProductOptionId() {
        return ProductOptionId.of(this.id);
    }

    public void update(String name, Integer additionalPrice, Integer sort, boolean soldOut, boolean visible) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.visible = visible;
    }
}
