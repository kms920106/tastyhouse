package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 전화번호 항목")
public record ShopPhoneNumberItem(
    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "대표번호 여부", example = "true")
    boolean primary,

    @Schema(description = "가상번호(안심번호) 여부", example = "false")
    boolean virtual
) {
    public static ShopPhoneNumberItem from(
        String phoneNumber,
        boolean primary,
        boolean virtual
    ) {
        return new ShopPhoneNumberItem(
            phoneNumber,
            primary,
            virtual
        );
    }
}
