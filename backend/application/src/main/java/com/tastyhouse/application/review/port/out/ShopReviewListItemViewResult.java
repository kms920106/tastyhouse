package com.tastyhouse.application.review.port.out;

/**
 * 점주 리뷰 목록 항목 — 조회 결과에 답변 가능 기간을 더한 형태.
 *
 * <p><b>챕터 09</b>에서 신설. 사유는 {@link ShopReviewReplyWindow} 참고. 그 밖의 표현 파생
 * (16자리 리뷰 번호 포맷·enum 문자열 강등)은 표현 계약이 수행한다.
 */
public record ShopReviewListItemViewResult(
    ShopReviewManagementListItemResult review,
    ShopReviewReplyWindow replyWindow
) {
}
