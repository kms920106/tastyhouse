package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

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

    public static ShopOrderNoticeResponse empty() {
        return new ShopOrderNoticeResponse(null, false, null);
    }
}
