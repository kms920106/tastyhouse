package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 이미지 변경 승인요청 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductImageChangeRequest}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ProductImageChangeRequestMapper}가 수행한다.
 */
@Entity
@Table(name = "PRODUCT_IMAGE_CHANGE_REQUEST")
public class ProductImageChangeRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ProductImageChangeRequestJpaEntity() {
    }

    private ProductImageChangeRequestJpaEntity(
        Long productId,
        Long imageFileId,
        ApprovalStatus status,
        String rejectReason
    ) {
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductImageChangeRequestMapper#toEntity}에서만 호출한다.
     */
    static ProductImageChangeRequestJpaEntity create(
        Long productId,
        Long imageFileId,
        ApprovalStatus status,
        String rejectReason
    ) {
        return new ProductImageChangeRequestJpaEntity(productId, imageFileId, status, rejectReason);
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

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
