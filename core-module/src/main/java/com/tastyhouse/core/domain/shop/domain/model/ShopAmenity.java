package com.tastyhouse.core.domain.shop.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
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

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "SHOP_AMENITY", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "shop_amenity_category_id"})})
public class ShopAmenity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "shop_amenity_category_id", nullable = false)
    private Long shopAmenityCategoryId; // 편의시설 카테고리 ID (SHOP_AMENITY_CATEGORY.id 참조)

    private ShopAmenity(
        Long shopId,
        Long shopAmenityCategoryId
    ) {
        this.shopId = shopId;
        this.shopAmenityCategoryId = shopAmenityCategoryId;
    }

    public static ShopAmenity of(
        Long shopId,
        Long shopAmenityCategoryId
    ) {
        return new ShopAmenity(
            shopId,
            shopAmenityCategoryId
        );
    }
}
