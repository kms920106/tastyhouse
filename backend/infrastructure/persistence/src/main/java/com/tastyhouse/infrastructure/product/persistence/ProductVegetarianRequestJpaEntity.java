package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 채식 설정 승인요청 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductVegetarianRequest}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ProductVegetarianRequestMapper}가 수행한다.
 */
@Entity
@Table(name = "PRODUCT_VEGETARIAN_REQUEST")
public class ProductVegetarianRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vegetarian_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private VegetarianType vegetarianType;

    @Column(name = "ingredients", nullable = false, length = 1000)
    private String ingredients;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ProductVegetarianRequestJpaEntity() {
    }

    private ProductVegetarianRequestJpaEntity(
        Long productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        ApprovalStatus status,
        String rejectReason
    ) {
        this.productId = productId;
        this.vegetarianType = vegetarianType;
        this.ingredients = ingredients;
        this.description = description;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductVegetarianRequestMapper#toEntity}에서만 호출한다.
     */
    static ProductVegetarianRequestJpaEntity create(
        Long productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        ApprovalStatus status,
        String rejectReason
    ) {
        return new ProductVegetarianRequestJpaEntity(
            productId, vegetarianType, ingredients, description, status, rejectReason
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변 필드는 건드리지 않는다.
     */
    void applyChanges(ApprovalStatus status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public VegetarianType getVegetarianType() {
        return this.vegetarianType;
    }

    public String getIngredients() {
        return this.ingredients;
    }

    public String getDescription() {
        return this.description;
    }

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
