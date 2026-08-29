package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopOrderNoticeHideCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 주문안내 게시중단 요청.
 *
 * <p>사유가 필수인 이유는 그것이 감사 기록이 아니라 <b>점주에게 그대로 전달되는 안내</b>이기 때문이다 —
 * 주문안내는 승인 절차가 없어 점주가 "왜 내려갔는지"를 알 수 있는 유일한 경로가 이 사유다(점주 조회
 * C-1의 {@code hiddenReason}으로 내려간다).
 *
 * <p>최대 길이는 {@code SHOP_ORDER_NOTICE.hidden_reason}의 {@code VARCHAR(500)} 안에서 잡는다.
 */
@Schema(description = "주문안내 게시중단 요청")
public record ShopOrderNoticeHideRequest(
    @NotBlank(message = "게시중단 사유는 필수입니다.")
    @Size(max = 500, message = "게시중단 사유는 최대 500자까지 입력할 수 있습니다.")
    @Schema(description = "게시중단 사유 (최대 500자)", example = "배민 외 결제 유도 문구가 포함되어 있습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public ShopOrderNoticeHideCommand toCommand(Long shopId) {
        return new ShopOrderNoticeHideCommand(shopId, reason);
    }
}
