package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.application.shop.port.in.ShopIntroductionUpdateCommand;

@Schema(description = "내 가게 소개(사장님 한마디) 등록 요청")
public record ShopIntroductionUpdateRequest(
    @NotBlank(message = "가게소개 메시지는 필수입니다.")
    @Schema(description = "가게소개 메시지 (최대 500자)", example = "정성을 다해 만드는 맛있는 분식집입니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String message
) {

    public ShopIntroductionUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopIntroductionUpdateCommand(ceoId, shopId, message());
    }
}
