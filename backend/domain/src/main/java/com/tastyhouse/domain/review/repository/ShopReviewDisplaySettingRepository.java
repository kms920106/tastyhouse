package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 리뷰 노출 정렬 설정 write 포트.
 *
 * <p>{@code findByShopId}는 upsert(없으면 생성, 있으면 갱신) 경로에서 대상을 로드하는 용도라 write 포트에
 * 남는다. <b>web-api의 조회 전용 소비는 이 포트를 쓰지 않는다</b> — QueryService에 write 포트를 주입하면
 * CQRS 교차 주입 금지 규칙을 어기므로 {@code ShopReviewDisplaySettingQueryDao}로 읽는다.
 */
public interface ShopReviewDisplaySettingRepository {

    Optional<ShopReviewDisplaySetting> findByShopId(ShopId shopId);

    ShopReviewDisplaySetting save(ShopReviewDisplaySetting shopReviewDisplaySetting);
}
