package com.tastyhouse.core.domain.shop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.infrastructure.persistence.converter.MemberIdConverter;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(name = "SHOP_BOOKMARK")
public class ShopBookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId; // 회원 ID (MEMBER.id 참조)

    protected ShopBookmark() {
    }

    public ShopBookmark(Long shopId, MemberId memberId) {
        this.shopId = shopId;
        this.memberId = memberId;
    }
}
