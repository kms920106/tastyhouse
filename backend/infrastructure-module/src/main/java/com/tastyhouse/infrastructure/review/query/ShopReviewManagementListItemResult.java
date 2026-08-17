package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 점주 리뷰 목록 항목.
 *
 * <p>{@code imageUrls}·{@code productNames}는 리뷰당 다건이라 목록 쿼리에 join하면 행이 불어난다. 따라서
 * 본 쿼리에서는 빈 목록으로 투영한 뒤 {@link #withImageUrls}/{@link #withProductNames}로 채운다 —
 * 컴포넌트 순서를 건드리지 않는 위더(wither)만 쓰므로, 같은 {@code String} 타입 필드가 위치 기반
 * 재조립에서 조용히 뒤바뀌는 사고를 원천 차단한다.
 *
 * <p>{@code orderMethod}는 {@code REVIEW.order_id}가 {@code NULL}인 미인증 리뷰에서 {@code null}이다.
 */
public record ShopReviewManagementListItemResult(
    Long id,
    String memberNickname,
    Double totalRating,
    String content,
    List<String> imageUrls,
    List<String> productNames,
    OrderMethod orderMethod,
    boolean hidden,
    boolean ownerOnly,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt,
    ReviewBlindStatus blindRequestStatus,
    LocalDateTime createdAt
) {

    public ShopReviewManagementListItemResult withImageUrls(List<String> imageUrls) {
        return new ShopReviewManagementListItemResult(
            this.id,
            this.memberNickname,
            this.totalRating,
            this.content,
            imageUrls,
            this.productNames,
            this.orderMethod,
            this.hidden,
            this.ownerOnly,
            this.ownerReplyContent,
            this.ownerReplyCreatedAt,
            this.blindRequestStatus,
            this.createdAt
        );
    }

    public ShopReviewManagementListItemResult withProductNames(List<String> productNames) {
        return new ShopReviewManagementListItemResult(
            this.id,
            this.memberNickname,
            this.totalRating,
            this.content,
            this.imageUrls,
            productNames,
            this.orderMethod,
            this.hidden,
            this.ownerOnly,
            this.ownerReplyContent,
            this.ownerReplyCreatedAt,
            this.blindRequestStatus,
            this.createdAt
        );
    }
}
