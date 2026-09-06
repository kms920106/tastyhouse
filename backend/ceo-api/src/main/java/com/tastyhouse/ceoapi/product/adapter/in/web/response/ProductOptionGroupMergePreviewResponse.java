package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionGroupMergePreviewResult;

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
    public static ProductOptionGroupMergePreviewResponse from(ProductOptionGroupMergePreviewResult result) {
        return new ProductOptionGroupMergePreviewResponse(
            ProductOptionGroupMergePreviewGroupResponse.from(result.base()),
            result.candidates().stream()
                .map(ProductOptionGroupMergePreviewGroupResponse::from)
                .toList(),
            result.mergeable(),
            result.blockedReason()
        );
    }
}
