package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;

/**
 * 메뉴그룹(점주 관리 화면).
 *
 * <p>손님 메뉴판과 달리 <b>숨긴 그룹도 포함</b>해 내려온다 — 이 화면이 숨김 상태 자체를 조작하기
 * 때문에 필터를 걸면 숨긴 그룹을 되살릴 방법이 없어진다.
 */
@Schema(description = "메뉴그룹")
public record ProductCategoryResponse(
    @Schema(description = "메뉴그룹 ID", example = "10")
    Long id,

    @Schema(description = "메뉴그룹명", example = "인기 메뉴")
    String name,

    @Schema(description = "메뉴그룹 설명. 미설정이면 null", example = "가장 많이 주문한 메뉴예요")
    String description,

    @Schema(description = "노출 순서(0부터). 순서 변경 시 서버가 0..N-1로 정규화한다.", example = "0")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    Boolean visible,

    @Schema(description = "소속된 메뉴 수(삭제된 메뉴 제외). 0이 아니면 이 그룹은 삭제할 수 없다.",
        example = "5")
    Long productCount
) {

    public static ProductCategoryResponse from(ProductCategoryManagementResult result) {
        return new ProductCategoryResponse(
            result.id(),
            result.name(),
            result.description(),
            result.sort(),
            result.visible(),
            result.productCount()
        );
    }
}
