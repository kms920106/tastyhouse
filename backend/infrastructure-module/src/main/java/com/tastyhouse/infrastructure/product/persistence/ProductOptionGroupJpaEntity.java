package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상품 옵션 그룹 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductOptionGroup}과 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductOptionGroupMapper}가 수행한다.
 */
@Entity
@Table(name = "PRODUCT_OPTION_GROUP")
public class ProductOptionGroupJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

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

    /**
     * 옵션그룹 유형. {@code @Enumerated(STRING)} + {@code columnDefinition = "VARCHAR(20)"}가 함께
     * 필요하다 — Hibernate 6의 {@code MySQLDialect}는 STRING enum을 네이티브 {@code ENUM(...)}으로
     * 기대하므로, {@code columnDefinition}이 없으면 {@code ddl-auto: validate}가 부팅을 거부한다.
     *
     * <p>{@code applyChanges}에 포함하지 않는다 — 유형 전환 경로를 두지 않기로 한 도메인 결정
     * ({@code ProductOptionGroup.groupType}이 {@code final}인 이유)을 영속 계층에서도 그대로 지킨다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ProductOptionGroupType groupType;

    protected ProductOptionGroupJpaEntity() {
    }

    private ProductOptionGroupJpaEntity(
        Long productId,
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductOptionGroupMapper#toEntity}에서만 호출한다.
     */
    static ProductOptionGroupJpaEntity create(
        Long productId,
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
        return new ProductOptionGroupJpaEntity(
            productId, name, description, required, multipleSelect, minSelect, maxSelect, sort, visible,
            groupType
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

    public Long getProductId() {
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
}
