package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeSuggestionResult;

@Schema(description = "옵션그룹 합치기 추천 묶음")
public record ProductOptionGroupMergeSuggestionResponse(
    @Schema(description = "동일성 서명(불투명 토큰). 제외 요청에 그대로 실어 보냅니다.",
        example = "3f2a...64자")
    String signature,

    @Schema(description = "묶음 대표 옵션그룹명", example = "메인 토핑 선택")
    String name,

    @Schema(description = "최소 선택 개수", example = "1")
    Integer minSelect,

    @Schema(description = "최대 선택 개수", example = "3")
    Integer maxSelect,

    @Schema(description = "이 묶음의 옵션그룹 수", example = "14")
    Integer groupCount,

    @Schema(description = "이 묶음이 걸린 메뉴 총 수", example = "27")
    Integer linkedProductCount,

    @Schema(description = "묶음 공통 옵션 대표 1세트")
    List<ProductOptionGroupMergeSuggestionOptionResponse> options,

    @Schema(description = "이 묶음에 속한 옵션그룹들")
    List<ProductOptionGroupMergeSuggestionGroupResponse> groups
) {
    public static ProductOptionGroupMergeSuggestionResponse from(ProductOptionGroupMergeSuggestionResult result) {
        return new ProductOptionGroupMergeSuggestionResponse(
            result.signature(),
            result.name(),
            result.minSelect(),
            result.maxSelect(),
            result.groupCount(),
            result.linkedProductCount(),
            result.options().stream()
                .map(ProductOptionGroupMergeSuggestionOptionResponse::from)
                .toList(),
            result.groups().stream()
                .map(ProductOptionGroupMergeSuggestionGroupResponse::from)
                .toList()
        );
    }
}
