package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 리뷰 노출 정렬 설정 — 고객 앱 리뷰 탭의 <b>기본</b> 정렬을 점주가 정한다.
 *
 * <p>설정 행이 없으면 {@link ReviewSortType#LATEST}로 간주한다(원문 "기본 적용값은 최신순"). 행을 미리
 * 만들지 않으므로 기존 가게에 대한 백필이 필요 없다.
 *
 * <p><b>{@code Shop}에 컬럼을 추가하지 않는다</b> — 리뷰 표시 설정이 앞으로 늘어날 여지가 있고
 * {@code Shop}은 이미 필드가 19개다.
 *
 * <p>이 설정은 고객이 정렬을 직접 지정하지 <b>않았을 때만</b> 적용된다 — 고객이 앱에서 추천순·최신순을
 * 바꿔 볼 수 있어야 하므로 점주 설정이 명시적 선택을 덮어써서는 안 된다.
 */
public class ShopReviewDisplaySetting {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private ReviewSortType sortType;
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopReviewDisplaySetting(Long id, ShopId shopId, ReviewSortType sortType, LocalDateTime updatedAt) {
        this.id = id;
        this.shopId = shopId;
        this.sortType = sortType;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 설정을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopReviewDisplaySetting of(ShopId shopId, ReviewSortType sortType) {
        return new ShopReviewDisplaySetting(null, shopId, sortType, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopReviewDisplaySetting reconstitute(
        Long id,
        ShopId shopId,
        ReviewSortType sortType,
        LocalDateTime updatedAt
    ) {
        return new ShopReviewDisplaySetting(id, shopId, sortType, updatedAt);
    }

    public void changeSortType(ReviewSortType sortType) {
        this.sortType = sortType;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ReviewSortType getSortType() {
        return this.sortType;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
