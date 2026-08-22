package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 매장 가격 인증 요청 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code StorePriceVerification}과 분리된 영속 전용 엔티티다. DB 매핑만 담당하고
 * 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code StorePriceVerificationMapper}가 수행한다.
 *
 * <p><b>테이블명이 {@code SHOP_} 접두를 유지하는 이유</b>: 요청이 <b>가게 단위</b>로 접수되기 때문이며,
 * 소유 컨텍스트(product)와는 별개다. 애그리거트가 product로 옮겨졌어도 테이블명을 바꾸지 않으므로
 * {@code @Table(name = ...)}로 명시 매핑한다.
 */
@Entity
@Table(name = "SHOP_STORE_PRICE_VERIFICATION")
public class StorePriceVerificationJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "price_list_file_id", nullable = false)
    private Long priceListFileId; // 매장 가격표 이미지 (UPLOADED_FILE.id)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private StorePriceVerificationStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason; // nullable — REJECTED일 때만

    @Column(name = "requested_by_ceo_id")
    private Long requestedByCeoId; // nullable — admin 대행 접수 경로면 null

    @Column(name = "processed_at")
    private LocalDateTime processedAt; // nullable — 접수 직후 null

    protected StorePriceVerificationJpaEntity() {
    }

    private StorePriceVerificationJpaEntity(
        Long shopId,
        Long priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        this.shopId = shopId;
        this.priceListFileId = priceListFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code StorePriceVerificationMapper#toEntity}에서만
     * 호출한다.
     */
    static StorePriceVerificationJpaEntity create(
        Long shopId,
        Long priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        return new StorePriceVerificationJpaEntity(
            shopId,
            priceListFileId,
            status,
            rejectReason,
            requestedByCeoId,
            processedAt
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·
     * 불변 필드({@code shop_id}·{@code price_list_file_id}·{@code requested_by_ceo_id})는 건드리지 않는다.
     */
    void applyChanges(
        StorePriceVerificationStatus status,
        String rejectReason,
        LocalDateTime processedAt
    ) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getPriceListFileId() {
        return this.priceListFileId;
    }

    public StorePriceVerificationStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public Long getRequestedByCeoId() {
        return this.requestedByCeoId;
    }

    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }
}
