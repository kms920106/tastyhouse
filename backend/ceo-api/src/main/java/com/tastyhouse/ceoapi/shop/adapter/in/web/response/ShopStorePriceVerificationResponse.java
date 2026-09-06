package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopStorePriceVerificationViewResult;

@Schema(description = "매장 가격 인증 현황")
public record ShopStorePriceVerificationResponse(
    @Schema(description = "최근 인증 요청 ID. 한 번도 요청하지 않았으면 null", example = "5")
    Long id,

    @Schema(description = "최근 인증 요청 상태. 미요청이면 null", example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "현재 매장 가격 인증 여부. 매장가·픽업가 설정 가능 여부의 근거입니다", example = "false")
    boolean verified,

    @Schema(description = "반려 사유. 반려 상태가 아니면 null", example = "가격표 이미지가 흐려 확인할 수 없습니다.")
    String rejectReason,

    @Schema(description = "인증을 충족하지 못한 메뉴 목록(인증 OFF 사유 표시용)")
    List<ShopStorePriceUnverifiedItemResponse> unverifiedItems
) {
    public static ShopStorePriceVerificationResponse from(ShopStorePriceVerificationViewResult result) {
        return new ShopStorePriceVerificationResponse(
            result.id(),
            result.status(),
            result.verified(),
            result.rejectReason(),
            result.unverifiedItems().stream()
                .map(ShopStorePriceUnverifiedItemResponse::from)
                .toList()
        );
    }
}
