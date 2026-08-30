package com.tastyhouse.application.review.port.out;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewSortType;

/**
 * 가게 리뷰 노출 설정 조회 포트(CQRS query 측 아웃바운드 포트) — 점주 관리 화면용.
 *
 * <p>점주가 자기 가게의 리뷰 정렬 설정을 확인하는 조회를 담당한다. 설정값 자체({@code ReviewSortType})
 * 외에 설정 존재 여부와 갱신 시각을 담은 {@link ShopReviewSortTypeResult}까지 함께 본다. 회원 화면
 * 조회는 {@link ShopReviewDisplaySettingQueryPort}가 소유한다.
 *
 * <p>{@link #findSortTypeByShopId}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 */
public interface ShopReviewDisplaySettingOwnerQueryPort {

    /** 공유 메서드 — {@link ShopReviewDisplaySettingQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ReviewSortType> findSortTypeByShopId(Long shopId);

    Optional<ShopReviewSortTypeResult> findSortTypeSettingByShopId(Long shopId);
}
