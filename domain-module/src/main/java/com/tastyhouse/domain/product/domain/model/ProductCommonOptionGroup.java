package com.tastyhouse.domain.product.domain.model;

import lombok.Getter;

import com.tastyhouse.domain.product.domain.vo.ProductId;

/**
 * 상품 공통 옵션 그룹 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductCommonOptionGroupJpaEntity} + {@code ProductCommonOptionGroupMapper}가 담당한다.
 */
@Getter
public class ProductCommonOptionGroup {

    private final Long id;
    private final ProductId productId;
    private String name;
    private String description;
    private boolean required;
    private boolean multipleSelect;
    private Integer minSelect;
    private Integer maxSelect;
    private Integer sort;
    private boolean visible;

    private ProductCommonOptionGroup(
        Long id,
        ProductId productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.required = required;
        this.multipleSelect = multipleSelect;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductCommonOptionGroup of(
        ProductId productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        return new ProductCommonOptionGroup(
            null, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductCommonOptionGroup reconstitute(
        Long id,
        ProductId productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        return new ProductCommonOptionGroup(
            id, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible
        );
    }

    public void update(
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.multipleSelect = multipleSelect;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.sort = sort;
        this.visible = visible;
    }
}
