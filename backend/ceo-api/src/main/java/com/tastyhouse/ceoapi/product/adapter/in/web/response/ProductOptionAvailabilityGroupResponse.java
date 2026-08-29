package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 옵션그룹 단위로 묶은 품절·숨김 관리 목록.
 *
 * <p>일반 옵션그룹과 공통 옵션그룹을 <b>하나의 목록으로 합쳐</b> 내려주고, 각 그룹이 어느 갈래인지
 * {@code optionType}으로 표시한다(손님 화면 옵션 조회가 두 갈래를 합쳐 내려주는 것과 같은 방식).
 */
@Schema(description = "품절·숨김 관리 옵션그룹")
public record ProductOptionAvailabilityGroupResponse(
    @Schema(description = "옵션그룹 ID", example = "20")
    Long optionGroupId,

    @Schema(description = "옵션그룹 종류", example = "NORMAL", allowableValues = {"NORMAL", "COMMON"})
    String optionType,

    @Schema(description = "옵션그룹명", example = "사이즈 선택")
    String name,

    @Schema(description = "필수 선택 여부. 화면의 [필수] 배지에 쓰인다.", example = "true")
    boolean required,

    @Schema(description = "최소 선택 개수. 품절·숨김 부분실패 제약의 기준이다.", example = "1")
    Integer minSelect,

    @Schema(description = "최대 선택 개수", example = "3")
    Integer maxSelect,

    @Schema(description = "이 옵션그룹에 연결된 메뉴명 목록")
    List<String> linkedProductNames,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "이 그룹에 속한 옵션 목록")
    List<ProductOptionAvailabilityItemResponse> options
) {

    public static ProductOptionAvailabilityGroupResponse from(
        Long optionGroupId,
        String optionType,
        String name,
        boolean required,
        Integer minSelect,
        Integer maxSelect,
        List<String> linkedProductNames,
        Integer sort,
        List<ProductOptionAvailabilityItemResponse> options
    ) {
        return new ProductOptionAvailabilityGroupResponse(
            optionGroupId,
            optionType,
            name,
            required,
            minSelect,
            maxSelect,
            linkedProductNames,
            sort,
            options
        );
    }
}
