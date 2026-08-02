package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.shop.domain.model.SuspensionReason;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점 영업 임시중지 JPA 영속 모델. 순수 도메인 모델 {@code ShopSuspension}과 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_SUSPENSION")
public class ShopSuspensionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private SuspensionReason reason; // 임시중지 사유

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", length = 20, columnDefinition = "VARCHAR(20)")
    private OrderMethod orderMethod; // 대상 주문유형 (null이면 전체 주문유형 대상)

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt; // 임시중지 시작 시각

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt; // 임시중지 종료 시각

    @Column(name = "released_at")
    private LocalDateTime releasedAt; // 해제 시각 (null이면 미해제)

    protected ShopSuspensionJpaEntity() {
    }

    private ShopSuspensionJpaEntity(
        ShopId shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt
    ) {
        this.shopId = shopId;
        this.reason = reason;
        this.orderMethod = orderMethod;
        this.startAt = startAt;
        this.endAt = endAt;
        this.releasedAt = releasedAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopSuspensionMapper#toEntity}에서만 호출한다.
     */
    static ShopSuspensionJpaEntity create(
        ShopId shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt
    ) {
        return new ShopSuspensionJpaEntity(shopId, reason, orderMethod, startAt, endAt, releasedAt);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public SuspensionReason getReason() {
        return this.reason;
    }

    public OrderMethod getOrderMethod() {
        return this.orderMethod;
    }

    public LocalDateTime getStartAt() {
        return this.startAt;
    }

    public LocalDateTime getEndAt() {
        return this.endAt;
    }

    public LocalDateTime getReleasedAt() {
        return this.releasedAt;
    }
}
