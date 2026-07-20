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

/**
 * 상점 배너 이미지 JPA 영속 모델. 순수 도메인 모델 {@code ShopBannerImage}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_BANNER_IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopBannerImageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 배너 이미지 파일 ID (FILE.id 참조)

    @Column(name = "sort")
    private Integer sort; // 정렬 순서

    private ShopBannerImageJpaEntity(Long shopId, Long imageFileId, Integer sort) {
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    static ShopBannerImageJpaEntity create(Long shopId, Long imageFileId, Integer sort) {
        return new ShopBannerImageJpaEntity(shopId, imageFileId, sort);
    }
}
