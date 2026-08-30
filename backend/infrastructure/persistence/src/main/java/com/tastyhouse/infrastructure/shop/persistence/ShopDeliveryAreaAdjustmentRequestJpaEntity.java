package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 프랜차이즈 배달지역 조정 신청 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopDeliveryAreaAdjustmentRequest}와 분리된 영속 전용 엔티티다. DB 매핑만
 * 담당하고 비즈니스 행위는 갖지 않으며, 도메인↔엔티티 변환은
 * {@code ShopDeliveryAreaAdjustmentRequestMapper}가 수행한다.
 *
 * <p>{@code status}에 {@code columnDefinition = "VARCHAR(20)"}가 필수다 — Hibernate 6의 MySQLDialect는
 * {@code EnumType.STRING}을 네이티브 {@code ENUM} 컬럼으로 매핑하므로, 생략하면 DB의 {@code VARCHAR(20)}과
 * 불일치해 {@code ddl-auto=validate}에서 부팅이 실패한다({@code BugReport} 장애 선례).
 */
@Entity
@Table(
    name = "SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST",
    indexes = {
        @Index(name = "idx_shop_delivery_area_adjustment_shop_id_status", columnList = "shop_id, status"),
        @Index(name = "idx_shop_delivery_area_adjustment_status", columnList = "status")
    }
)
public class ShopDeliveryAreaAdjustmentRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "counterpart_shop_name", nullable = false)
    private String counterpartShopName;

    @Column(name = "counterpart_business_number", nullable = false, length = 12)
    private String counterpartBusinessNumber;

    @Column(name = "franchise_name", nullable = false)
    private String franchiseName;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "consent_file_id", nullable = false)
    private Long consentFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DeliveryAreaAdjustmentStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopDeliveryAreaAdjustmentRequestJpaEntity() {
    }

    private ShopDeliveryAreaAdjustmentRequestJpaEntity(
        Long shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        Long consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason
    ) {
        this.shopId = shopId;
        this.counterpartShopName = counterpartShopName;
        this.counterpartBusinessNumber = counterpartBusinessNumber;
        this.franchiseName = franchiseName;
        this.reason = reason;
        this.consentFileId = consentFileId;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음).
     * {@code ShopDeliveryAreaAdjustmentRequestMapper#toEntity}에서만 호출한다.
     */
    static ShopDeliveryAreaAdjustmentRequestJpaEntity create(
        Long shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        Long consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason
    ) {
        return new ShopDeliveryAreaAdjustmentRequestJpaEntity(
            shopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            consentFileId,
            status,
            rejectReason
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변
     * 필드는 건드리지 않는다 — 신청 내용은 접수 후 바뀌지 않고 상태 전이만 일어난다.
     */
    void applyChanges(DeliveryAreaAdjustmentStatus status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getCounterpartShopName() {
        return this.counterpartShopName;
    }

    public String getCounterpartBusinessNumber() {
        return this.counterpartBusinessNumber;
    }

    public String getFranchiseName() {
        return this.franchiseName;
    }

    public String getReason() {
        return this.reason;
    }

    public Long getConsentFileId() {
        return this.consentFileId;
    }

    public DeliveryAreaAdjustmentStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
