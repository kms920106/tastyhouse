package com.tastyhouse.application.shop.port.out;

/**
 * 메뉴모음컷 투영 — 손님 노출용.
 *
 * <p>{@code status}·{@code rejectReason}을 <b>담지 않는다.</b> 손님 화면은 승인분만 보므로 상태 필드가
 * 무의미하고, 반려 사유는 점주에게만 의미 있는 내부 정보다 — 투영 단계에서 아예 뽑지 않아 응답 조립
 * 실수로 새어 나갈 경로를 없앤다.
 */
public record ShopMenuCollectionImageExposureResult(
    Long id,
    String imageUrl,
    Integer sort
) {
}
