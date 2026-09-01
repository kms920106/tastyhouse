package com.tastyhouse.adminapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.OptionGroupResult;

@Schema(description = "상품 옵션그룹 응답")
public record ProductOptionGroupResponse(
    @Schema(description = "옵션그룹 ID", example = "1")
    Long id,

    @Schema(description = "옵션그룹명", example = "맵기 선택")
    String name,

    @Schema(description = "옵션그룹 설명", example = "매운맛 정도를 선택하세요")
    String description,

    @Schema(description = "필수 선택 여부", example = "true")
    boolean required,

    @Schema(description = "복수 선택 가능 여부", example = "false")
    boolean multipleSelect,

    @Schema(description = "최소 선택 개수", example = "1")
    Integer minSelect,

    @Schema(description = "최대 선택 개수", example = "1")
    Integer maxSelect,

    @Schema(description = "공통 옵션그룹 여부(여러 상품 공유)", example = "false")
    boolean common,

    @Schema(description = "옵션그룹 유형. CUP_DEPOSIT은 일회용컵 보증금 그룹입니다.",
        example = "NORMAL", allowableValues = {"NORMAL", "CUP_DEPOSIT"})
    String groupType,

    @Schema(description = "옵션 목록")
    List<ProductOptionResponse> options
) {
    public static ProductOptionGroupResponse from(OptionGroupResult result) {
        List<ProductOptionResponse> options = result.options().stream()
            .map(ProductOptionResponse::from)
            .toList();
        return new ProductOptionGroupResponse(
            result.id(),
            result.name(),
            result.description(),
            result.required(),
            result.multipleSelect(),
            result.minSelect(),
            result.maxSelect(),
            result.common(),
            result.groupType(),
            options
        );
    }
}
