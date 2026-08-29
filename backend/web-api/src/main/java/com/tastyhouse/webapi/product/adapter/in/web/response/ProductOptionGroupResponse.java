package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션 그룹")
public record ProductOptionGroupResponse(
    @Schema(description = "옵션 그룹 ID", example = "1")
    Long id,

    @Schema(description = "옵션 그룹명", example = "맵기 선택")
    String name,

    @Schema(description = "옵션 그룹 설명", example = "원하시는 맵기를 선택해주세요")
    String description,

    @Schema(description = "필수 선택 여부", example = "true")
    boolean required,

    @Schema(description = "복수 선택 가능 여부", example = "false")
    boolean multipleSelect,

    @Schema(description = "최소 선택 개수", example = "1")
    Integer minSelect,

    @Schema(description = "최대 선택 개수", example = "1")
    Integer maxSelect,

    @Schema(description = "공통 옵션 여부", example = "false")
    boolean common,

    @Schema(description = "옵션그룹 유형. CUP_DEPOSIT은 일회용컵 보증금 그룹으로, 프론트가 별도 섹션으로 "
        + "렌더링하고 금액을 '보증금'으로 표기해야 한다. common(공유 여부)과는 다른 축이다.",
        example = "NORMAL", allowableValues = {"NORMAL", "CUP_DEPOSIT"})
    String groupType,

    @Schema(description = "옵션 목록")
    List<ProductOptionResponse> options
) {
    public static ProductOptionGroupResponse from(
        Long id,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        boolean common,
        String groupType,
        List<ProductOptionResponse> options
    ) {
        return new ProductOptionGroupResponse(
            id,
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            common,
            groupType,
            options
        );
    }
}
