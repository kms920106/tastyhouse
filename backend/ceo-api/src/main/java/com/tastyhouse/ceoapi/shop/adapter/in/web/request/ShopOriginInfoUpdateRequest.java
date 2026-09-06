package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.shop.port.in.ShopOriginInfoUpdateCommand;

@Schema(description = "내 가게 원산지 표시 등록/수정 요청")
public record ShopOriginInfoUpdateRequest(
    @NotBlank(message = "원산지 입력 방식은 필수입니다.")
    @Schema(description = "입력 방식", example = "DIRECT", allowableValues = {"DIRECT", "FRANCHISE_URL"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String sourceType,

    @Size(max = 2000, message = "원산지 정보는 2000자 이하여야 합니다.")
    @Schema(description = "직접 입력 본문. sourceType=DIRECT일 때 필수다.",
        example = "돼지고기: 국내산, 쇠고기: 미국산, 닭고기: 국내산")
    String content,

    @Size(max = 500, message = "본사 제공 URL은 500자 이하여야 합니다.")
    @Schema(description = "본사 제공 URL. sourceType=FRANCHISE_URL일 때 필수이며 http:// 또는 https://로 시작해야 한다.",
        example = "https://example.com/origin")
    String url
) {
    public ShopOriginInfoUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopOriginInfoUpdateCommand(ceoId, shopId, sourceType(), content(), url());
    }
}
