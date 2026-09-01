package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

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

    public static ShopOriginInfoResponse from(ShopOriginInfoResult result) {
        return new ShopOriginInfoResponse(
            result.sourceType(),
            result.content(),
            result.url(),
            result.updatedAt()
        );
    }

    /**
     * 원산지 정보를 아직 등록하지 않은 가게의 응답. 출처 유형만 기본값({@code DIRECT})으로 채운다
     * (챕터 09에서 QueryService의 기본값 조립을 이 표현 계약으로 옮겼다).
     */
    public static ShopOriginInfoResponse empty() {
        return new ShopOriginInfoResponse(
            OriginSourceType.DIRECT.name(),
            null,
            null,
            null
        );
    }
}
