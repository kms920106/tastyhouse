package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 가게 요청 조회 포트(CQRS query 측 아웃바운드 포트) — 점주 화면용.
 *
 * <p>점주가 올린 요청의 목록·상세와 요청 유형별 상세(이미지 변경·권역 조정·리뷰 블라인드)를 조회한다.
 * 관리자 검수 화면 조회는 {@link ShopRequestManagementQueryPort}가 소유한다.
 *
 * <p>{@link #findRequestDetail}·{@link #findComments}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라
 * 양쪽에 선언만 중복한다 — 요청 본문과 댓글은 올린 쪽과 검수하는 쪽이 같은 것을 본다.
 */
public interface ShopRequestQueryPort {

    PageResult<ShopRequestListItemResult> findRequestPage(ShopRequestSearchCondition condition, PageQuery pageQuery);

    /** 공유 메서드 — {@link ShopRequestManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ShopRequestDetailResult> findRequestDetail(Long requestId);

    Optional<ShopRequestImageChangeDetailResult> findImageChangeDetail(Long sourceRequestId);

    Optional<ShopRequestAdjustmentDetailResult> findAdjustmentDetail(Long sourceRequestId);

    Optional<ShopRequestReviewBlindDetailResult> findReviewBlindDetail(Long sourceRequestId);

    /** 공유 메서드 — {@link ShopRequestManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<ShopRequestCommentResult> findComments(Long requestId);
}
