package com.tastyhouse.webapi.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션 그룹")
public record OptionGroupResponse(
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

    @Schema(description = "옵션 목록")
    List<OptionResponse> options
) {
    public static OptionGroupResponse from(
        Long id,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        boolean common,
        List<OptionResponse> options
    ) {
        return new OptionGroupResponse(
            id,
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            common,
            options
        );
    }
}
