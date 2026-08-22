package com.tastyhouse.ceoapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 내 가게 원산지 표시 등록/수정 요청.
 *
 * <p><b>{@code content}·{@code url}에 Bean Validation의 필수 검증을 걸지 않는다.</b> 두 필드의 필수
 * 여부가 {@code sourceType}에 따라 갈리는 조건부 제약이라, 어느 한쪽에 {@code @NotBlank}를 붙이면 다른
 * 방식으로 저장하는 정상 요청이 400으로 막힌다. 조건부 판정은 도메인({@code ShopOriginInfo})이 수행하고
 * 스펙이 약속한 {@code code}({@code SHOP_ORIGIN_CONTENT_REQUIRED} 등)로 응답한다 — 길이 제약만
 * 여기서 미리 걸러 낸다.
 */
@Schema(description = "내 가게 원산지 표시 등록/수정 요청")
public record ShopOriginInfoUpdateRequest(
    @NotBlank(message = "원산지 입력 방식은 필수입니다.")
    @Schema(description = "입력 방식", example = "DIRECT", allowableValues = {"DIRECT", "FRANCHISE_URL"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String sourceType,

    @Size(max = 2000, message = "원산지 정보는 2000자 이하여야 합니다.")
    @Schema(description = "직접 입력 본문. sourceType=DIRECT일 때 필수다.",
        example = "돼지고기: 국내산, 쇠고기: 미국산, 닭고기: 국내산")
    String content,

    @Size(max = 500, message = "본사 제공 URL은 500자 이하여야 합니다.")
    @Schema(description = "본사 제공 URL. sourceType=FRANCHISE_URL일 때 필수이며 http:// 또는 https://로 시작해야 한다.",
        example = "https://example.com/origin")
    String url
) {
}
