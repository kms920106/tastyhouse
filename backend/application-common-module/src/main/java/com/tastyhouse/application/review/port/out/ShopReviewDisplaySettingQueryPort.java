package com.tastyhouse.application.review.port.out;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewSortType;

/**
 * 가게 리뷰 노출 설정 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>가게 상세의 리뷰 목록을 어떤 순서로 보여줄지 결정하는 정렬 기준만 조회한다. 점주가 그 설정을
 * 확인·변경하는 화면의 조회는 {@link ShopReviewDisplaySettingOwnerQueryPort}가 소유한다.
 *
 * <p>{@link #findSortTypeByShopId}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다 —
 * 점주도 자기 화면의 미리보기에서 회원과 같은 정렬 기준을 적용한다.
 */
public interface ShopReviewDisplaySettingQueryPort {

    /** 공유 메서드 — {@link ShopReviewDisplaySettingOwnerQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ReviewSortType> findSortTypeByShopId(Long shopId);
}
