package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

/**
 * 손님 화면에 노출되는 주문안내 응답 — 메뉴 정보 최상단에 표시되는 주문 전 안내 문구.
 *
 * <p>ceo 응답과 달리 {@code hidden}·{@code hiddenReason}을 담지 않는다 — 게시중단된 문구는 이 응답이
 * 아예 만들어지지 않고 {@code data: null}로 내려가므로, 손님 응답에 게시중단 상태를 실을 이유가 없다.
 * 관리자 조치 사유를 손님에게 노출하지 않아야 한다는 요구도 함께 만족한다("실제 쓰는 필드만" 원칙).
 *
 * <p>식별자도 담지 않는다 — 주문안내는 가게당 1건이고 손님이 개별 조작할 대상이 아니다.
 */
@Schema(description = "가게 주문안내 응답")
public record ShopOrderNoticeResponse(
    @Schema(description = "주문안내 본문", example = "포장 주문은 매장에서 10분 정도 소요됩니다.")
    String content
) {
    public static ShopOrderNoticeResponse from(ShopOrderNoticeResult result) {
        return new ShopOrderNoticeResponse(result.content());
    }
}
