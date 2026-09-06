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
    /**
     * 옵션그룹 유형. <b>{@code final}이다 — 유형 전환 경로를 두지 않는다.</b> 일반↔보증금을 바꾸면
     * 과거 주문 스냅샷의 해석이 소급해서 달라진다(그 주문의 옵션이 추가금이었는지 보증금이었는지가
     * 뒤집힌다). {@code update}가 이 값을 아예 받지 않는 것이 그 차단 장치다.
     */
    private final ProductOptionGroupType groupType;

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
        boolean visible,
        ProductOptionGroupType groupType
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
        this.groupType = groupType != null ? groupType : ProductOptionGroupType.NORMAL;
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
        boolean visible,
        ProductOptionGroupType groupType
    ) {
        return new ProductOptionGroup(
            null, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible, groupType
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
        boolean visible,
        ProductOptionGroupType groupType
    ) {
        return new ProductOptionGroup(
            id, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible, groupType
        );
    }

    public ProductOptionGroupId getProductOptionGroupId() {
        return ProductOptionGroupId.of(this.id);
    }

    /**
     * 옵션그룹의 이름·설명·선택 제약·순서·노출을 변경한다.
     *
     * <p><b>{@code groupType}을 받지 않는다.</b> 유형 전환은 과거 주문 스냅샷의 해석을 소급 변경하므로,
     * "바꿀 수 있는데 막는" 것이 아니라 <b>바꿀 경로 자체를 두지 않는다</b>(hide에 대응하는 un-hide를
     * 두지 않은 것과 같은 형태).
     */
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

    public ProductOptionGroupType getGroupType() {
        return this.groupType;
    }

    /** 일회용컵 보증금 옵션그룹인가. 금액 계산·검증 분기의 단일 판정점이다. */
    public boolean isCupDeposit() {
        return this.groupType.isCupDeposit();
    }
}
