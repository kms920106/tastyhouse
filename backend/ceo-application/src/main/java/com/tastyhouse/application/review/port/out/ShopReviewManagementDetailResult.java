package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 점주 리뷰 상세.
 *
 * <p>{@code shopId}는 응답에 노출하지 않지만, 소비 Service가 "이 리뷰가 내 가게 것인지"를 재검증하는 데
 * 쓴다 — 경로의 {@code shopId}만 믿으면 남의 가게 리뷰 상세를 열 수 있는 IDOR이 된다.
 *
 * <p>다건 필드({@code imageUrls}·{@code productNames}·{@code tagNames}·{@code blindRequests})는 목록과
 * 같은 이유로 별도 조회 후 위더로 채운다.
 *
 * <p><b>{@code deliveryRating}/{@code deliveryComment}(배달 평가)는 이 ceo 전용 Result에만 있다</b> —
 * 원문 규격상 고객 앱에 노출되지 않으므로 web용 {@code ReviewDetailResult}에는 넣지 않는다. Result가
 * 소비자별로 분리돼 있어 실수로 새기 어렵다.
 */
public record ShopReviewManagementDetailResult(
    Long id,
    Long shopId,
    String memberNickname,
    Double totalRating,
    String content,
    List<String> imageUrls,
    List<String> productNames,
    OrderMethod orderMethod,
    boolean hidden,
    boolean ownerOnly,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating,
    boolean willRevisit,
    List<String> tagNames,
    Long ownerReplyId,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt,
    LocalDateTime ownerReplyUpdatedAt,
    List<ReviewBlindRequestHistoryResult> blindRequests,
    LocalDateTime createdAt,
    Integer deliveryRating,
    String deliveryComment
) {

    public ShopReviewManagementDetailResult withCollections(
        List<String> imageUrls,
        List<String> productNames,
        List<String> tagNames,
        List<ReviewBlindRequestHistoryResult> blindRequests
    ) {
        return new ShopReviewManagementDetailResult(
            this.id,
            this.shopId,
            this.memberNickname,
            this.totalRating,
            this.content,
            imageUrls,
            productNames,
            this.orderMethod,
            this.hidden,
            this.ownerOnly,
            this.tasteRating,
            this.amountRating,
            this.priceRating,
            this.atmosphereRating,
            this.kindnessRating,
            this.hygieneRating,
            this.willRevisit,
            tagNames,
            this.ownerReplyId,
            this.ownerReplyContent,
            this.ownerReplyCreatedAt,
            this.ownerReplyUpdatedAt,
            blindRequests,
            this.createdAt,
            this.deliveryRating,
            this.deliveryComment
        );
    }
}
