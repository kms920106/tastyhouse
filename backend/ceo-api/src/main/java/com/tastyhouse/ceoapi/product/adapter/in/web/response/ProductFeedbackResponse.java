package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;

/**
 * 점주 화면의 고객 의견 한 줄 — 메뉴 × 유형으로 묶은 지난 한 주 집계.
 *
 * <p><b>제보자 정보를 담지 않는다.</b> 점주가 특정 손님을 식별하면 보복 우려가 있고, 제보의 목적은
 * 정보 수정이지 손님 응대가 아니다.
 */
@Schema(description = "메뉴 정보 고객 의견 집계")
public record ProductFeedbackResponse(

    @Schema(description = "메뉴 ID(클릭 시 메뉴 상세로 이동)", example = "100")
    Long productId,

    @Schema(description = "메뉴명", example = "후라이드 치킨")
    String productName,

    @Schema(description = "의견 유형", example = "PRICE",
        allowableValues = {"PRICE", "IMAGE", "COMPOSITION", "SOLD_OUT", "ETC"})
    String feedbackType,

    @Schema(description = "지난 한 주 동안 같은 유형으로 접수된 건수", example = "3")
    Integer count,

    @Schema(description = "ETC 유형의 서술 내용(최대 10건). 그 외 유형이면 빈 배열입니다")
    List<String> contents
) {

    public static ProductFeedbackResponse from(ProductFeedbackSummaryResult result) {
        return new ProductFeedbackResponse(
            result.productId(),
            result.productName(),
            result.feedbackType().name(),
            result.count(),
            result.contents()
        );
    }
}
