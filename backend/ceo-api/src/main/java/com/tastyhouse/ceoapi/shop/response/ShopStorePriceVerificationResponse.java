package com.tastyhouse.ceoapi.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 가게의 매장 가격 인증 현황 — 최근 요청 상태와 현재 인증 플래그를 한 응답에 담는다.
 *
 * <p><b>{@code verified}와 {@code status}는 서로 다른 축이며 합칠 수 없다.</b> 인증은 승인 후에도
 * 배달가가 매장가를 넘어서면 자동으로 해제되므로, 최근 요청이 {@code APPROVED}인데
 * {@code verified=false}인 상태가 정상적으로 존재한다. 화면은 매장가·픽업가 입력 가능 여부를
 * {@code verified}로, 진행 중 안내(대기·검수 중·반려 사유)를 {@code status}로 판단한다.
 *
 * <p>한 번도 요청하지 않은 가게는 {@code id}·{@code status}·{@code rejectReason}이 모두 {@code null}이고
 * {@code verified}만 유효하다 — 미요청을 별도 상태값으로 만들지 않는 이유는, 그러면 도메인 enum에
 * 없는 값이 응답 계약에 섞여 프론트가 서버 enum과 화면 상수를 따로 관리해야 하기 때문이다.
 */
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

    public static ShopStorePriceVerificationResponse from(
        Long id,
        String status,
        boolean verified,
        String rejectReason,
        List<ShopStorePriceUnverifiedItemResponse> unverifiedItems
    ) {
        return new ShopStorePriceVerificationResponse(id, status, verified, rejectReason, unverifiedItems);
    }
}
