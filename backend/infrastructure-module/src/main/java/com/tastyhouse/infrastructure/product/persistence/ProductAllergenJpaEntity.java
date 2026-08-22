package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 알레르기 유발성분 JPA 영속 모델. 순수 도메인 모델 {@code ProductAllergen}과 분리된 영속 전용 엔티티다.
 *
 * <p>{@code applyChanges}가 없다 — 목록이 replace-all로 교체되므로 행을 수정하는 경로가 없고, 기존 행을
 * 지운 뒤 새로 넣는다.
 */
@Entity
@Table(name = "PRODUCT_ALLERGEN")
public class ProductAllergenJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품 ID (PRODUCT.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "allergen_type", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private AllergenType allergenType; // 유발성분 (MILK, EGG, BUCKWHEAT, ...)

    protected ProductAllergenJpaEntity() {
    }

    private ProductAllergenJpaEntity(Long productId, AllergenType allergenType) {
        this.productId = productId;
        this.allergenType = allergenType;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductAllergenMapper#toEntity}에서만 호출한다.
     */
    static ProductAllergenJpaEntity create(Long productId, AllergenType allergenType) {
        return new ProductAllergenJpaEntity(productId, allergenType);
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public AllergenType getAllergenType() {
        return this.allergenType;
    }
}
