package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

/**
 * 가게 원산지 표시 정보(손님 화면).
 *
 * <p>점주 응답과 달리 {@code updatedAt}을 담지 않는다 — 손님에게 최종 수정 시각은 의미가 없다.
 * 미설정이면 이 응답 자체가 내려가지 않고 {@code data: null}이며, 화면은 그때 원산지 영역을 통째로
 * 감춘다(점주 화면이 빈 폼용 기본값을 받는 것과 반대다).
 */
@Schema(description = "가게 원산지 표시 정보")
public record ShopOriginInfoResponse(
    @Schema(description = "입력 방식", example = "DIRECT", allowableValues = {"DIRECT", "FRANCHISE_URL"})
    String sourceType,

    @Schema(description = "직접 입력 본문. sourceType=FRANCHISE_URL이면 null",
        example = "돼지고기: 국내산, 쇠고기: 미국산")
    String content,

    @Schema(description = "본사 제공 URL. sourceType=DIRECT이면 null", example = "https://example.com/origin")
    String url
) {

    public static ShopOriginInfoResponse from(ShopOriginInfoResult result) {
        return new ShopOriginInfoResponse(
            result.sourceType(),
            result.content(),
            result.url()
        );
    }
}
