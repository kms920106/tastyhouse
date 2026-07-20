package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 상점-편의시설 배정 JPA 영속 모델. 순수 도메인 모델 {@code ShopAmenity}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_AMENITY", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "shop_amenity_category_id"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopAmenityJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "shop_amenity_category_id", nullable = false)
    private Long shopAmenityCategoryId; // 편의시설 카테고리 ID (SHOP_AMENITY_CATEGORY.id 참조)

    private ShopAmenityJpaEntity(Long shopId, Long shopAmenityCategoryId) {
        this.shopId = shopId;
        this.shopAmenityCategoryId = shopAmenityCategoryId;
    }

    static ShopAmenityJpaEntity create(Long shopId, Long shopAmenityCategoryId) {
        return new ShopAmenityJpaEntity(shopId, shopAmenityCategoryId);
    }
}
