package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 변경이력 순수 도메인 모델(append-only).
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopChangeHistoryJpaEntity} + {@code ShopChangeHistoryMapper}가 담당한다. 기록 후에는 바뀌지
 * 않으므로 전 필드가 final이고 상태전이 메서드가 없다.
 *
 * <p>1행의 단위는 "점주가 저장 버튼을 1번 누른 것"(mutation 엔드포인트 1회 호출)이다. 배달팁 구간처럼
 * 컬렉션을 통째로 교체하는 변경도 행마다 쪼개지 않고 1행으로 남기며, 값에는 컬렉션 전체 스냅샷을 담는다.
 *
 * <p>{@code previousValue}/{@code newValue}를 JSON이 아니라 요약 문자열로 담는 이유는 이력이 append-only
 * 불변 데이터라서다 — 구조화해 두면 조회 시점의 파싱 책임이 과거 스키마 버전 전부를 알아야 하는 문제로
 * 번지므로, 기록 시점의 표현을 그 자리에서 굳힌다.
 */
public class ShopChangeHistory {

    private final Long id;
    private final ShopId shopId;
    private final ShopChangeCategory category;
    private final ShopChangeType changeType;
    private final ShopChangeActionType actionType;
    private final ShopChangeActorType actorType;
    private final Long actorId; // CEO.id 또는 ADMIN.id
    private final String previousValue; // 변경 전 요약 (nullable — 등록 시 null)
    private final String newValue; // 변경 후 요약 (nullable — 삭제 시 null)
    private final LocalDateTime createdAt;

    private ShopChangeHistory(
        Long id,
        ShopId shopId,
        ShopChangeCategory category,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActorType actorType,
        Long actorId,
        String previousValue,
        String newValue,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.category = category;
        this.changeType = changeType;
        this.actionType = actionType;
        this.actorType = actorType;
        this.actorId = actorId;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    /**
     * 신규 이력을 만든다. 대분류는 중분류에서 파생하므로 따로 받지 않는다 — 두 값이 어긋날 수 없다.
     */
    public static ShopChangeHistory of(
        ShopId shopId,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActor actor,
        String previousValue,
        String newValue
    ) {
        return new ShopChangeHistory(null, shopId, changeType.getCategory(), changeType, actionType,
            actor.actorType(), actor.actorId(), previousValue, newValue, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopChangeHistory reconstitute(
        Long id,
        ShopId shopId,
        ShopChangeCategory category,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActorType actorType,
        Long actorId,
        String previousValue,
        String newValue,
        LocalDateTime createdAt
    ) {
        return new ShopChangeHistory(id, shopId, category, changeType, actionType, actorType, actorId,
            previousValue, newValue, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopChangeCategory getCategory() {
        return this.category;
    }

    public ShopChangeType getChangeType() {
        return this.changeType;
    }

    public ShopChangeActionType getActionType() {
        return this.actionType;
    }

    public ShopChangeActorType getActorType() {
        return this.actorType;
    }

    public Long getActorId() {
        return this.actorId;
    }

    public String getPreviousValue() {
        return this.previousValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
