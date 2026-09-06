package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

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

    public static ShopOriginInfoResponse empty() {
        return new ShopOriginInfoResponse(
            OriginSourceType.DIRECT.name(),
            null,
            null,
            null
        );
    }
}
