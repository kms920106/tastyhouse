package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 가게 소개(사장님 한마디) 응답")
public record ShopIntroductionResponse(
    @Schema(description = "가게소개 메시지 (등록 이력이 없으면 null)", example = "정성을 다해 만드는 맛있는 분식집입니다.")
    String message
) {
    /**
     * 등록 이력이 없으면 {@code message}가 null인 응답이 된다 — {@code Result}가 없는 조회의 정상
     * 형태이므로 원시값을 그대로 받는다(챕터 09).
     */
    public static ShopIntroductionResponse from(String message) {
        return new ShopIntroductionResponse(message);
    }
}
