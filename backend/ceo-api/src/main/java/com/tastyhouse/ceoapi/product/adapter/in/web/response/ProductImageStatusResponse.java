package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductImageStatusResult;

/**
 * 메뉴 이미지 목록 + 검수 상태.
 *
 * <p>반영된 이미지({@code images})와 검수 중·과거 요청({@code requests})을 함께 내려준다 — 점주가
 * "올렸는데 왜 안 보이나"를 한 화면에서 판단할 수 있어야 한다.
 */
@Schema(description = "메뉴 이미지 현황")
public record ProductImageStatusResponse(
    @Schema(description = "반영된 이미지 목록(정렬 순)")
    List<ProductImageResponse> images,

    @Schema(description = "이미지 등록 요청 목록(최근 순)")
    List<ProductImageChangeRequestResponse> requests
) {

    public static ProductImageStatusResponse from(ProductImageStatusResult result) {
        return new ProductImageStatusResponse(
            result.images().stream().map(ProductImageResponse::from).toList(),
            result.requests().stream().map(ProductImageChangeRequestResponse::from).toList()
        );
    }
}
