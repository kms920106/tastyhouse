package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 리뷰 노출 정렬 설정 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopReviewDisplaySetting}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사
 * 필드)만 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopReviewDisplaySettingMapper}가
 * 수행한다.
 */
@Entity
@Table(name = "SHOP_REVIEW_DISPLAY_SETTING")
public class ShopReviewDisplaySettingJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReviewSortType sortType;

    protected ShopReviewDisplaySettingJpaEntity() {
    }

    private ShopReviewDisplaySettingJpaEntity(Long shopId, ReviewSortType sortType) {
        this.shopId = shopId;
        this.sortType = sortType;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopReviewDisplaySettingMapper#toEntity}에서만 호출한다.
     */
    static ShopReviewDisplaySettingJpaEntity create(Long shopId, ReviewSortType sortType) {
        return new ShopReviewDisplaySettingJpaEntity(shopId, sortType);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변 필드는
     * 건드리지 않는다.
     */
    void applyChanges(ReviewSortType sortType) {
        this.sortType = sortType;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public ReviewSortType getSortType() {
        return this.sortType;
    }
}
