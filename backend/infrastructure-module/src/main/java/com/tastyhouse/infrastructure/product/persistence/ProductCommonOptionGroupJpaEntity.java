package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상품 공통 옵션 그룹 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductCommonOptionGroup}과 분리된 영속 전용 엔티티다. 도메인↔엔티티
 * 변환은 {@code ProductCommonOptionGroupMapper}가 수행한다.
 */
@Entity
@Table(name = "PRODUCT_COMMON_OPTION_GROUP")
public class ProductCommonOptionGroupJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ProductIdConverter.class)
    @Column(name = "product_id", nullable = false)
    private ProductId productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "is_multiple_select", nullable = false)
    private boolean multipleSelect;

    @Column(name = "min_select")
    private Integer minSelect;

    @Column(name = "max_select")
    private Integer maxSelect;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected ProductCommonOptionGroupJpaEntity() {
    }

    private ProductCommonOptionGroupJpaEntity(
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductCommonOptionGroupMapper#toEntity}에서만 호출한다.
     */
    static ProductCommonOptionGroupJpaEntity create(
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
        return new ProductCommonOptionGroupJpaEntity(
            productId, name, description, required, multipleSelect, minSelect, maxSelect, sort, visible
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). productId는 건드리지 않는다.
     */
    void applyChanges(
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
