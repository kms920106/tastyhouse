package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 사장님 한마디 이력 JPA 영속 모델. 순수 도메인 모델 {@code ShopOwnerMessageHistory}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_OWNER_MESSAGE_HISTORY")
public class ShopOwnerMessageHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // 사장님 한마디 메시지 내용

    protected ShopOwnerMessageHistoryJpaEntity() {
    }

    private ShopOwnerMessageHistoryJpaEntity(Long shopId, String message) {
        this.shopId = shopId;
        this.message = message;
    }

    static ShopOwnerMessageHistoryJpaEntity create(Long shopId, String message) {
        return new ShopOwnerMessageHistoryJpaEntity(shopId, message);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getMessage() {
        return this.message;
    }
}
