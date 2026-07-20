package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점 사진 카테고리 JPA 영속 모델. 순수 도메인 모델 {@code ShopPhotoCategory}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_PHOTO_CATEGORY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopPhotoCategoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 사진 카테고리명 (예: 가게 외관, 메뉴, 내부 인테리어)

    private ShopPhotoCategoryJpaEntity(Long shopId, String name) {
        this.shopId = shopId;
        this.name = name;
    }

    static ShopPhotoCategoryJpaEntity create(Long shopId, String name) {
        return new ShopPhotoCategoryJpaEntity(shopId, name);
    }

    void applyChanges(String name) {
        this.name = name;
    }
}
