package com.tastyhouse.infrastructure.shop.query;

/**
 * 주문안내 조회 결과.
 *
 * <p>감사 시각을 담지 않는다 — 점주 화면(C-1)도 손님 화면(C-3)도 등록/수정 시각을 표시하지 않고,
 * 주문안내는 가게당 1건이라 목록 정렬 기준으로도 쓰이지 않는다("실제 쓰는 필드만" 원칙).
 *
 * <p>{@code hidden}·{@code hiddenReason}은 점주 화면 전용이다. 손님 화면은 게시중단된 문구를 아예
 * 받지 않으므로({@code data: null}) 이 두 필드가 손님 응답 record로 넘어가지 않는다.
 */
public record ShopOrderNoticeResult(
    Long id,
    Long shopId,
    String content,
    boolean hidden,
    String hiddenReason
) {

}
