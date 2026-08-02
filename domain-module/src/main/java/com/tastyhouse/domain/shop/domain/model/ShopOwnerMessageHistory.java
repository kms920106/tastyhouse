package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 사장님 한마디 이력 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopOwnerMessageHistoryJpaEntity} + {@code ShopOwnerMessageHistoryMapper}가 담당한다.
 * 최신 메시지 조회가 {@code createdAt} 내림차순 정렬에 의존하므로 감사 시각을 필드로 유지한다.
 */
public class ShopOwnerMessageHistory {

    private final Long id;
    private final ShopId shopId;
    private final String message;
    private final LocalDateTime createdAt;

    private ShopOwnerMessageHistory(Long id, ShopId shopId, String message, LocalDateTime createdAt) {
        this.id = id;
        this.shopId = shopId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public static ShopOwnerMessageHistory of(ShopId shopId, String message) {
        return new ShopOwnerMessageHistory(null, shopId, message, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopOwnerMessageHistory reconstitute(Long id, ShopId shopId, String message, LocalDateTime createdAt) {
        return new ShopOwnerMessageHistory(id, shopId, message, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getMessage() {
        return this.message;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
