package com.tastyhouse.ceoapi.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 내 가게 원산지 표시 정보.
 *
 * <p>미설정 가게도 {@code data}가 {@code null}이 아니라 {@code sourceType=DIRECT}·{@code content=null}로
 * 내려간다 — 화면이 분기 없이 빈 폼을 그릴 수 있게 하려는 것이다. 손님 응답({@code web-api})은 반대로
 * 미설정이면 {@code data: null}을 내려 원산지 영역을 통째로 감춘다.
 */
@Schema(description = "내 가게 원산지 표시 정보")
public record ShopOriginInfoResponse(
    @Schema(description = "입력 방식", example = "DIRECT", allowableValues = {"DIRECT", "FRANCHISE_URL"})
    String sourceType,

    @Schema(description = "직접 입력 본문. sourceType=FRANCHISE_URL이거나 미설정이면 null",
        example = "돼지고기: 국내산, 쇠고기: 미국산")
    String content,

    @Schema(description = "본사 제공 URL. sourceType=DIRECT이거나 미설정이면 null",
        example = "https://example.com/origin")
    String url,

    @Schema(description = "최종 수정 일시. 미설정이면 null")
    LocalDateTime updatedAt
) {

    public static ShopOriginInfoResponse from(
        String sourceType,
        String content,
        String url,
        LocalDateTime updatedAt
    ) {
        return new ShopOriginInfoResponse(
            sourceType,
            content,
            url,
            updatedAt
        );
    }
}
