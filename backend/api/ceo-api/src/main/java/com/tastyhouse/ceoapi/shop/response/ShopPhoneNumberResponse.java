package com.tastyhouse.ceoapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 전화번호 응답")
public record ShopPhoneNumberResponse(
    @Schema(description = "전화번호 ID", example = "1")
    Long id,

    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "대표 여부", example = "true")
    boolean primary,

    @Schema(description = "가상번호 여부", example = "false")
    boolean virtual
) {
    public static ShopPhoneNumberResponse from(
        Long id,
        String phoneNumber,
        boolean primary,
        boolean virtual
    ) {
        return new ShopPhoneNumberResponse(
            id,
            phoneNumber,
            primary,
            virtual
        );
    }
}
