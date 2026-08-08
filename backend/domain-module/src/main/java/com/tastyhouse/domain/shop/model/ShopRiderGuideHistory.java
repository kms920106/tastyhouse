package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 라이더 안내 문구 변경 이력 순수 도메인 모델(append-only).
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopRiderGuideHistoryJpaEntity} + {@code ShopRiderGuideHistoryMapper}가 담당한다.
 * 점주 변경({@code UPDATE})과 관리자 검수 조치({@code REVISION_REQUEST}/{@code DELETION})를
 * 한 테이블에 기록하며, 기록 후에는 바뀌지 않으므로 전 필드가 final이고 상태전이 메서드가 없다.
 */
public class ShopRiderGuideHistory {

    private final Long id;
    private final ShopId shopId;
    private final RiderGuideActorType actorType;
    private final Long actorId; // CEO.id 또는 ADMIN.id
    private final RiderGuideActionType actionType;
    private final String previousVisitGuide; // 변경 전 문구 (nullable)
    private final String newVisitGuide; // 변경 후 문구 (nullable — 삭제 조치 시 null)
    private final String reason; // 관리자 조치 사유 (nullable, 점주 변경에서는 항상 null)
    private final LocalDateTime createdAt;

    private ShopRiderGuideHistory(
        Long id,
        ShopId shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.actionType = actionType;
        this.previousVisitGuide = previousVisitGuide;
        this.newVisitGuide = newVisitGuide;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static ShopRiderGuideHistory of(
        ShopId shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason
    ) {
        return new ShopRiderGuideHistory(null, shopId, actorType, actorId, actionType, previousVisitGuide,
            newVisitGuide, reason, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopRiderGuideHistory reconstitute(
        Long id,
        ShopId shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason,
        LocalDateTime createdAt
    ) {
        return new ShopRiderGuideHistory(id, shopId, actorType, actorId, actionType, previousVisitGuide,
            newVisitGuide, reason, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
