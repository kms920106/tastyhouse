package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * Product와 BBQ 메뉴 ID 매핑 JPA 영속 모델 — BBQ API 외부 메뉴 ID 임시 저장.
 *
 * <p>순수 도메인 모델 {@code ProductBbq}와 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductBbqMapper}가 수행한다.
 */
@Entity
@Table(name = "PRODUCT_BBQ")
public class ProductBbqJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "bbq_menu_id", nullable = false)
    private Long bbqMenuId;

    @Column(name = "bbq_category_id")
    private Long bbqCategoryId;

    @Column(name = "is_options_synced", nullable = false)
    private boolean optionsSynced;

    protected ProductBbqJpaEntity() {
    }

    private ProductBbqJpaEntity(Long productId, Long bbqMenuId, Long bbqCategoryId, boolean optionsSynced) {
        this.productId = productId;
        this.bbqMenuId = bbqMenuId;
        this.bbqCategoryId = bbqCategoryId;
        this.optionsSynced = optionsSynced;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductBbqMapper#toEntity}에서만 호출한다.
     */
    static ProductBbqJpaEntity create(Long productId, Long bbqMenuId, Long bbqCategoryId, boolean optionsSynced) {
        return new ProductBbqJpaEntity(productId, bbqMenuId, bbqCategoryId, optionsSynced);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체).
     */
    void applyChanges(boolean optionsSynced) {
        this.optionsSynced = optionsSynced;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getBbqMenuId() {
        return this.bbqMenuId;
    }

    public Long getBbqCategoryId() {
        return this.bbqCategoryId;
    }

    public boolean isOptionsSynced() {
        return this.optionsSynced;
    }
}
