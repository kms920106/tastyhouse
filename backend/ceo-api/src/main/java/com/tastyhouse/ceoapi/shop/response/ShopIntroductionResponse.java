package com.tastyhouse.ceoapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 가게 소개(사장님 한마디) 응답")
public record ShopIntroductionResponse(
    @Schema(description = "가게소개 메시지 (등록 이력이 없으면 null)", example = "정성을 다해 만드는 맛있는 분식집입니다.")
    String message
) {
    public static ShopIntroductionResponse from(String message) {
        return new ShopIntroductionResponse(message);
    }
}
