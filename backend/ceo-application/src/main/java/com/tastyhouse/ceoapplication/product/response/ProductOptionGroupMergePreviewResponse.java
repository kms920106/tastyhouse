package com.tastyhouse.ceoapplication.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 합치기 미리보기(상세보기 diff) 응답.
 *
 * <p>{@code mergeable}이 {@code false}면 합치기 버튼을 비활성화하고 {@code blockedReason}을 안내한다 —
 * 실행 시점에 거절되는 것보다, 되돌릴 수 없는 동작을 <b>누르기 전에</b> 막는 편이 낫다.
 */
@Schema(description = "옵션그룹 합치기 미리보기")
public record ProductOptionGroupMergePreviewResponse(
    @Schema(description = "기준(살아남을) 옵션그룹")
    ProductOptionGroupMergePreviewGroupResponse base,

    @Schema(description = "흡수될 후보 옵션그룹 목록(기준 제외)")
    List<ProductOptionGroupMergePreviewGroupResponse> candidates,

    @Schema(description = "사전 검증 통과 여부(합치기 버튼 활성 조건)", example = "true")
    Boolean mergeable,

    @Schema(description = "불가 사유 코드. mergeable=false일 때만 값이 있습니다.",
        example = "PRODUCT_OPTION_GROUP_MERGE_SAME_PRODUCT_LINKED")
    String blockedReason
) {

    public static ProductOptionGroupMergePreviewResponse from(
        ProductOptionGroupMergePreviewGroupResponse base,
        List<ProductOptionGroupMergePreviewGroupResponse> candidates,
        Boolean mergeable,
        String blockedReason
    ) {
        return new ProductOptionGroupMergePreviewResponse(
            base,
            candidates,
            mergeable,
            blockedReason
        );
    }
}
