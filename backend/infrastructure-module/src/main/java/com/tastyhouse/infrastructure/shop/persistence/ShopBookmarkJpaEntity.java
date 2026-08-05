package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점 북마크 JPA 영속 모델. 순수 도메인 모델 {@code ShopBookmark}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_BOOKMARK")
public class ShopBookmarkJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 회원 ID (MEMBER.id 참조)

    protected ShopBookmarkJpaEntity() {
    }

    private ShopBookmarkJpaEntity(Long shopId, Long memberId) {
        this.shopId = shopId;
        this.memberId = memberId;
    }

    static ShopBookmarkJpaEntity create(Long shopId, Long memberId) {
        return new ShopBookmarkJpaEntity(shopId, memberId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getMemberId() {
        return this.memberId;
    }
}
