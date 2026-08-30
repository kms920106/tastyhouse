package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 라이더 안내 문구 변경 이력 JPA 영속 모델(append-only). 순수 도메인 모델
 * {@code ShopRiderGuideHistory}와 분리된 영속 전용 엔티티다.
 *
 * <p>enum 필드는 {@code @Enumerated(EnumType.STRING)}과 {@code columnDefinition = "VARCHAR(20)"}을
 * 병기한다 — {@code columnDefinition}을 빠뜨리면 Hibernate 6의 {@code MySQLDialect}가 네이티브
 * {@code ENUM(...)}을 기대해 {@code ddl-auto=validate}에서 부팅이 실패한다.
 */
@Entity
@Table(name = "SHOP_RIDER_GUIDE_HISTORY")
public class ShopRiderGuideHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RiderGuideActorType actorType; // 변경 주체 (CEO, ADMIN)

    @Column(name = "actor_id", nullable = false)
    private Long actorId; // 변경 주체 ID (CEO.id 또는 ADMIN.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RiderGuideActionType actionType; // 조치 유형 (UPDATE, REVISION_REQUEST, DELETION)

    @Column(name = "previous_visit_guide", length = 200)
    private String previousVisitGuide; // 변경 전 문구

    @Column(name = "new_visit_guide", length = 200)
    private String newVisitGuide; // 변경 후 문구 (삭제 조치 시 NULL)

    @Column(name = "reason", length = 200)
    private String reason; // 관리자 조치 사유 (점주 변경 시 NULL)

    protected ShopRiderGuideHistoryJpaEntity() {
    }

    private ShopRiderGuideHistoryJpaEntity(
        Long shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason
    ) {
        this.shopId = shopId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.actionType = actionType;
        this.previousVisitGuide = previousVisitGuide;
        this.newVisitGuide = newVisitGuide;
        this.reason = reason;
    }

    static ShopRiderGuideHistoryJpaEntity create(
        Long shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason
    ) {
        return new ShopRiderGuideHistoryJpaEntity(shopId, actorType, actorId, actionType, previousVisitGuide,
            newVisitGuide, reason);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public RiderGuideActorType getActorType() {
        return this.actorType;
    }

    public Long getActorId() {
        return this.actorId;
    }

    public RiderGuideActionType getActionType() {
        return this.actionType;
    }

    public String getPreviousVisitGuide() {
        return this.previousVisitGuide;
    }

    public String getNewVisitGuide() {
        return this.newVisitGuide;
    }

    public String getReason() {
        return this.reason;
    }
}
