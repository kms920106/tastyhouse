package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

/**
 * 점주 주문안내 조회 응답.
 *
 * <p><b>식별자를 담지 않는다.</b> 주문안내는 가게당 1건이고 모든 조작이 {@code shopId} 경로로
 * 이루어지므로(PUT 전체교체, 관리자 hide/unhide) 프론트가 ID를 쓸 곳이 없다.
 *
 * <p>미설정 가게도 {@code data: null}이 아니라 {@code content: null}인 객체를 받는다 — 점주 화면은
 * 문구가 없어도 게시중단 여부를 함께 보여주는 편집 폼을 그리므로, 응답 자체가 없으면 프론트가 두
 * 가지 빈 상태(미설정 / 응답 없음)를 구분해야 한다.
 */
@Schema(description = "점주 주문안내 응답")
public record ShopOrderNoticeResponse(
    @Schema(description = "주문안내 본문 (미설정이면 null)", example = "포장 주문은 매장에서 10분 정도 소요됩니다.")
    String content,

    @Schema(description = "관리자 게시중단 여부", example = "false")
    boolean hidden,

    @Schema(description = "게시중단 사유 (게시중이면 null)", example = "외부 결제 유도 문구가 포함되어 있습니다.")
    String hiddenReason
) {
    public static ShopOrderNoticeResponse from(ShopOrderNoticeResult result) {
        return new ShopOrderNoticeResponse(result.content(), result.hidden(), result.hiddenReason());
    }

    /**
     * 아직 주문안내를 등록하지 않은 가게의 응답. 게시중단은 등록된 문구에만 걸리므로 항상 게시중이다.
     */
    public static ShopOrderNoticeResponse empty() {
        return new ShopOrderNoticeResponse(null, false, null);
    }
}
