package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴그룹(카테고리) 단위로 묶은 품절·숨김 관리 목록.
 *
 * <p>앱에 노출되는 순서와 동일하게 메뉴그룹 단위로 정렬된다.
 */
@Schema(description = "품절·숨김 관리 메뉴그룹")
public record ProductAvailabilityGroupResponse(
    @Schema(description = "카테고리 ID. 카테고리 미지정 메뉴는 null", example = "5")
    Long categoryId,

    @Schema(description = "카테고리명. null이면 화면에서 \"분류 없음\"으로 표시한다.", example = "분식")
    String categoryName,

    @Schema(description = "카테고리 정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "이 카테고리에 속한 메뉴 목록")
    List<ProductAvailabilityItemResponse> products
) {

    public static ProductAvailabilityGroupResponse from(
        Long categoryId,
        String categoryName,
        Integer sort,
        List<ProductAvailabilityItemResponse> products
    ) {
        return new ProductAvailabilityGroupResponse(
            categoryId,
            categoryName,
            sort,
            products
        );
    }
}
