package com.tastyhouse.ceoapplication.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 미리보기의 옵션그룹 1건(기준 또는 후보).
 *
 * <p>{@code *Differs} 플래그는 <b>기준 그룹 기준</b>이다 — 기준 자신은 항상 {@code false}다.
 * 합치면 기준값이 이기므로, 이 플래그가 켜진 항목이 곧 "합치면 바뀌는 것"이다.
 */
@Schema(description = "합치기 미리보기 옵션그룹")
public record ProductOptionGroupMergePreviewGroupResponse(
    @Schema(description = "옵션그룹 ID", example = "10")
    Long id,

    @Schema(description = "옵션그룹명", example = "메인 토핑 선택")
    String name,

    @Schema(description = "설명", example = "토핑을 골라주세요")
    String description,

    @Schema(description = "필수 선택 여부", example = "true")
    Boolean required,

    @Schema(description = "복수 선택 여부", example = "false")
    Boolean multipleSelect,

    @Schema(description = "최소 선택 개수", example = "1")
    Integer minSelect,

    @Schema(description = "최대 선택 개수", example = "3")
    Integer maxSelect,

    @Schema(description = "이 그룹이 연결된 메뉴명 목록")
    List<String> linkedProductNames,

    @Schema(description = "옵션그룹명이 기준과 다른가", example = "false")
    Boolean nameDiffers,

    @Schema(description = "최소 선택 개수가 기준과 다른가", example = "false")
    Boolean minSelectDiffers,

    @Schema(description = "최대 선택 개수가 기준과 다른가", example = "false")
    Boolean maxSelectDiffers,

    @Schema(description = "옵션 목록")
    List<ProductOptionGroupMergePreviewOptionResponse> options
) {

    public static ProductOptionGroupMergePreviewGroupResponse from(
        Long id,
        String name,
        String description,
        Boolean required,
        Boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        List<String> linkedProductNames,
        Boolean nameDiffers,
        Boolean minSelectDiffers,
        Boolean maxSelectDiffers,
        List<ProductOptionGroupMergePreviewOptionResponse> options
    ) {
        return new ProductOptionGroupMergePreviewGroupResponse(
            id,
            name,
            description,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            linkedProductNames,
            nameDiffers,
            minSelectDiffers,
            maxSelectDiffers,
            options
        );
    }
}
