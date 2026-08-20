package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 옵션 그룹 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductOptionGroupJpaEntity} + {@code ProductOptionGroupMapper}가 담당한다.
 */
public class ProductOptionGroup {

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

    private ProductOptionGroup(
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

    public static ProductOptionGroup of(
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
        return new ProductOptionGroup(
            null, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductOptionGroup reconstitute(
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
        return new ProductOptionGroup(
            id, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible
        );
    }

    public ProductOptionGroupId getProductOptionGroupId() {
        return ProductOptionGroupId.of(this.id);
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

    /**
     * 옵션그룹을 메뉴판에서 감춘다(소프트 삭제).
     *
     * <p>행을 지우지 않는 이유: 이 그룹에 속한 옵션들은 주문 시점에 {@code ORDER_PRODUCT_OPTION}으로
     * 박제되지만 그 스냅샷은 {@code option_group_id}를 함께 남긴다. 행을 하드 삭제하면 과거 주문의
     * 참조가 끊어지므로, 감추기만 해서 <b>과거 주문 이력은 보존하고 메뉴판에서만 제거</b>한다.
     *
     * <p>되살리는 전이 메서드는 두지 않는다 — 복구 UI가 없는 동안은 "변경 경로가 없다"를 구조로
     * 표현하는 편이 이 저장소의 원칙과 일관된다.
     */
    public void hide() {
        this.visible = false;
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isMultipleSelect() {
        return this.multipleSelect;
    }

    public Integer getMinSelect() {
        return this.minSelect;
    }

    public Integer getMaxSelect() {
        return this.maxSelect;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
