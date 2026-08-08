package com.tastyhouse.ceoapi.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 가게 주문가능 상태 응답 — 가게 전체 상태와 배정된 주문유형별 상태를 함께 담는다")
public record ShopOrderAvailabilityResponse(
    @Schema(description = "가게 주문가능 여부. 유형별 임시중지는 이 값에 영향을 주지 않는다", example = "true")
    boolean orderable,

    @Schema(description = "불가 사유 코드. 주문 가능하면 null", example = "OUT_OF_BUSINESS_HOURS")
    String unavailableReason,

    @Schema(description = "불가 사유 한글 문구. 주문 가능하면 null", example = "영업시간이 아닙니다")
    String unavailableReasonName,

    @Schema(description = "배정된 주문유형별 상태. 배정이 없으면 빈 배열")
    List<ShopOrderMethodAvailabilityResponse> orderMethods
) {

    public static ShopOrderAvailabilityResponse from(
        boolean orderable,
        String unavailableReason,
        String unavailableReasonName,
        List<ShopOrderMethodAvailabilityResponse> orderMethods
    ) {
        return new ShopOrderAvailabilityResponse(
            orderable,
            unavailableReason,
            unavailableReasonName,
            orderMethods
        );
    }
}
