package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주의 고객 의견 확인 시각 — 화면 아이콘의 빨간 점(미확인 표시) 판정 근거.
 *
 * <p><b>왜 {@code SHOP}에 컬럼을 붙이지 않고 별도 애그리거트인가</b>: 확인 시각은 가게 정보가 아니라
 * 점주의 열람 상태다. 가게에 붙이면 가게 정보를 수정하는 모든 경로가 이 값을 딸고 다니게 되고,
 * 열람이라는 잦은 쓰기가 가게 행을 계속 갱신하게 된다.
 *
 * <p>가게당 1건이며({@code UNIQUE(shop_id)}), 제보 접수 시각이 이 값보다 나중이면 미확인이다.
 * 확인 처리는 값을 현재 시각으로 밀어 올리는 것뿐이므로 전이 메서드는 {@link #markRead} 하나다.
 */
public class ProductFeedbackRead {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private LocalDateTime readAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductFeedbackRead(
        Long id,
        ShopId shopId,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductFeedbackRead of(ShopId shopId, LocalDateTime readAt) {
        return new ProductFeedbackRead(null, shopId, readAt, null, null);
    }

    public static ProductFeedbackRead reconstitute(
        Long id,
        ShopId shopId,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductFeedbackRead(id, shopId, readAt, createdAt, updatedAt);
    }

    /**
     * 확인 시각을 밀어 올린다.
     *
     * <p>과거 시각으로 되돌리지 않는다 — 동시에 열린 두 화면이 각자의 시각으로 호출할 때 늦게 도착한
     * 이전 시각이 최신 확인을 덮으면, 이미 읽은 제보에 빨간 점이 다시 켜진다.
     */
    public void markRead(LocalDateTime readAt) {
        if (this.readAt == null || readAt.isAfter(this.readAt)) {
            this.readAt = readAt;
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public LocalDateTime getReadAt() {
        return this.readAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
