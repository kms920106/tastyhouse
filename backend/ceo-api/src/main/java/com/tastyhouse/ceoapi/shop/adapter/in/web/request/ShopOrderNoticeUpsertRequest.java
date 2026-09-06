package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUpsertCommand;

@Schema(description = "주문안내 등록·수정 요청")
public record ShopOrderNoticeUpsertRequest(
    @NotBlank(message = "주문안내 내용을 입력해 주세요.")
    @Size(max = 500, message = "주문안내는 500자 이내로 입력해 주세요.")
    @Schema(description = "주문안내 본문 (1~500자)", example = "포장 주문은 매장에서 10분 정도 소요됩니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String content
) {
    public ShopOrderNoticeUpsertCommand toCommand(Long ceoId, Long shopId) {
        return new ShopOrderNoticeUpsertCommand(ceoId, shopId, content());
    }
}
