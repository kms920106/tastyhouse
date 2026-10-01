package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.ShopPhoneNumberCreateCommand;

@Schema(description = "가게 전화번호 등록 요청")
public record ShopPhoneNumberCreateRequest(
    @NotBlank(message = "전화번호는 필수입니다.")
    @Schema(description = "전화번호", example = "02-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber,

    @NotNull(message = "가상번호 여부는 필수입니다.")
    @Schema(description = "가상번호 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean virtual
) {

    public ShopPhoneNumberCreateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopPhoneNumberCreateCommand(ceoId, shopId, phoneNumber(), virtual());
    }
}
