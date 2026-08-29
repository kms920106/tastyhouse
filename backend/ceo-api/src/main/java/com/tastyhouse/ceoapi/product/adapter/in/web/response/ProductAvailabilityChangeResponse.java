package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 품절·숨김 일괄 처리 결과.
 *
 * <p><b>전체가 실패해도 HTTP 200 + {@code failed} 전량으로 응답한다</b> — 부분 성공과 전체 실패의 응답
 * 형태를 갈라놓으면 프론트가 두 경로를 타야 하고, "1건 실패"와 "전건 실패"의 화면 처리가 실제로는 같다
 * (성공 N건 / 실패 M건 안내). 요청 자체가 잘못된 경우(빈 배열·기간 범위 위반·소유권 위반·가게 미존재)만
 * 4xx다.
 */
@Schema(description = "품절·숨김 일괄 처리 결과")
public record ProductAvailabilityChangeResponse(
    @Schema(description = "적용된 대상 ID 목록")
    List<Long> succeededIds,

    @Schema(description = "적용하지 못한 대상과 그 사유")
    List<ProductAvailabilityFailureResponse> failed
) {

    public static ProductAvailabilityChangeResponse from(
        List<Long> succeededIds,
        List<ProductAvailabilityFailureResponse> failed
    ) {
        return new ProductAvailabilityChangeResponse(
            succeededIds,
            failed
        );
    }
}
