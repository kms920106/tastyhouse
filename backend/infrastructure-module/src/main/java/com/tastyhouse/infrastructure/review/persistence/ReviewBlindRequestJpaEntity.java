package com.tastyhouse.infrastructure.review.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 리뷰 게시중단 요청 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReviewBlindRequest}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ReviewBlindRequestMapper}가 수행한다.
 *
 * <p>도메인 모델은 {@code updatedAt}을 소비하지 않으므로 {@code BaseEntity}가 관리하는 값을 그대로 두되
 * {@code ReviewBlindRequestMapper#toDomain}이 {@code reconstitute}에 넘기지 않는다.
 */
@Entity
@Table(name = "REVIEW_BLIND_REQUEST")
public class ReviewBlindRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReviewBlindReason reason;

    @Column(name = "detail_reason", length = 500)
    private String detailReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReviewBlindStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "blind_until")
    private LocalDateTime blindUntil;

    protected ReviewBlindRequestJpaEntity() {
    }

    private ReviewBlindRequestJpaEntity(
        Long reviewId,
        Long shopId,
        Long ceoId,
        ReviewBlindReason reason,
        String detailReason,
        ReviewBlindStatus status,
        String rejectReason,
        LocalDateTime blindUntil
    ) {
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.reason = reason;
        this.detailReason = detailReason;
        this.status = status;
        this.rejectReason = rejectReason;
        this.blindUntil = blindUntil;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewBlindRequestMapper#toEntity}에서만 호출한다.
     */
    static ReviewBlindRequestJpaEntity create(
        Long reviewId,
        Long shopId,
        Long ceoId,
        ReviewBlindReason reason,
        String detailReason,
        ReviewBlindStatus status,
        String rejectReason,
        LocalDateTime blindUntil
    ) {
        return new ReviewBlindRequestJpaEntity(
            reviewId, shopId, ceoId, reason, detailReason, status, rejectReason, blindUntil
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변 필드는
     * 건드리지 않는다.
     */
    void applyChanges(ReviewBlindStatus status, String rejectReason, LocalDateTime blindUntil) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.blindUntil = blindUntil;
    }

    public Long getId() {
        return this.id;
    }

    public Long getReviewId() {
        return this.reviewId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getCeoId() {
        return this.ceoId;
    }

    public ReviewBlindReason getReason() {
        return this.reason;
    }

    public String getDetailReason() {
        return this.detailReason;
    }

    public ReviewBlindStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public LocalDateTime getBlindUntil() {
        return this.blindUntil;
    }
}
