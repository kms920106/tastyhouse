package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 점주 공지 첨부 이미지 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopNoticeImage}와 분리된 영속 전용 엔티티다. 불변 애그리거트라 update
 * 경로가 없어 {@code applyChanges}를 두지 않는다(수정은 replace-all).
 */
@Entity
@Table(
    name = "SHOP_NOTICE_IMAGE",
    indexes = {
        @Index(name = "idx_shop_notice_image_notice_id", columnList = "shop_notice_id")
    }
)
public class ShopNoticeImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_notice_id", nullable = false)
    private Long shopNoticeId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected ShopNoticeImageJpaEntity() {
    }

    private ShopNoticeImageJpaEntity(Long shopNoticeId, Long imageFileId, int sortOrder) {
        this.shopNoticeId = shopNoticeId;
        this.imageFileId = imageFileId;
        this.sortOrder = sortOrder;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopNoticeImageMapper#toEntity}에서만 호출한다.
     */
    static ShopNoticeImageJpaEntity create(Long shopNoticeId, Long imageFileId, int sortOrder) {
        return new ShopNoticeImageJpaEntity(shopNoticeId, imageFileId, sortOrder);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopNoticeId() {
        return this.shopNoticeId;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }
}
