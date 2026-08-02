package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
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

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId; // 가게 ID (SHOP.id 참조)

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId; // 회원 ID (MEMBER.id 참조)

    protected ShopBookmarkJpaEntity() {
    }

    private ShopBookmarkJpaEntity(ShopId shopId, MemberId memberId) {
        this.shopId = shopId;
        this.memberId = memberId;
    }

    static ShopBookmarkJpaEntity create(ShopId shopId, MemberId memberId) {
        return new ShopBookmarkJpaEntity(shopId, memberId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }
}
