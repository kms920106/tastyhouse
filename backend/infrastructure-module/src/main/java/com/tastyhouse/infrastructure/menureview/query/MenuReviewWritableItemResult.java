package com.tastyhouse.infrastructure.menureview.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 평가 가능 메뉴 목록 항목 — 한 주문의 주문 항목 중 평가 제외 대상이 아닌 것들.
 *
 * <p>이미 평가한 항목도 <b>목록에서 빼지 않고</b> {@code menuReviewId}·{@code rating}·{@code comment}를
 * 함께 내려준다 — 프론트가 폼 초기값을 채우고 "이미 평가함" 상태를 표시해야 하기 때문이다. 평가하지
 * 않았으면 이 세 필드가 전부 {@code null}이다.
 *
 * <p>{@code productImageUrl}은 저장 경로가 아니라 <b>표시용 URL</b>이다 — DAO가 fetch 직후
 * {@code FileUrlResolver}로 완성해 담는다(파일 URL 조립 위치 규칙).
 */
public record MenuReviewWritableItemResult(
    Long orderProductId,
    Long productId,
    String productName,
    String productImageUrl,
    Long menuReviewId,
    Integer rating,
    String comment
) {

    @QueryProjection
    public MenuReviewWritableItemResult {
    }

    /**
     * 이미지 슬롯만 교체한 사본 — 투영식에 URL 변환을 끼울 수 없어 fetch 직후 재조립한다.
     */
    public MenuReviewWritableItemResult withProductImageUrl(String productImageUrl) {
        return new MenuReviewWritableItemResult(
            this.orderProductId,
            this.productId,
            this.productName,
            productImageUrl,
            this.menuReviewId,
            this.rating,
            this.comment
        );
    }
}
