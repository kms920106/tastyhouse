package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;

@Schema(description = "가게 전화번호 항목")
public record ShopPhoneNumberItem(
    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "대표번호 여부", example = "true")
    boolean primary,

    @Schema(description = "가상번호(안심번호) 여부", example = "false")
    boolean virtual
) {
    public static ShopPhoneNumberItem from(ShopPhoneNumberResult result) {
        return new ShopPhoneNumberItem(
            result.phoneNumber(),
            result.primary(),
            result.virtual()
        );
    }
}
