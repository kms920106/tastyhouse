package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 도형 환산 미리보기 결과(저장하지 않음).
 *
 * <p>저장 전에 <b>무엇이 열리고 무엇이 닫히는지</b>를 모두 보여주는 것이 이 응답의 목적이다. 배달지역은
 * 주문 접수 가능 범위를 직접 바꾸므로, 점주가 결과를 모른 채 저장하면 매출에 바로 영향이 간다.
 *
 * <p>{@code blockedAdminDongs}가 비어 있지 않으면 저장이 409로 실패한다 — 미리 알려주므로 점주는 배달팁을
 * 먼저 정리할 수 있다.
 */
@Schema(description = "도형 환산 미리보기 결과")
public record ShopDeliveryAreaPolygonPreviewResponse(
    @Schema(description = "기준점에서 최원거리 정점까지의 거리(m)", example = "3800")
    int maxRadiusMeters,

    @Schema(description = "배달지역 최대 반경(7km) 이내인지", example = "true")
    boolean withinAllowedRadius,

    @Schema(description = "환산 결과 전체(저장하면 열려 있게 되는 행정동)")
    List<ShopDeliveryAreaCandidateResponse> adminDongs,

    @Schema(description = "저장하면 새로 열리는 행정동")
    List<ShopDeliveryAreaCandidateResponse> addedAdminDongs,

    @Schema(description = "저장하면 닫히는 행정동")
    List<ShopDeliveryAreaCandidateResponse> removedAdminDongs,

    @Schema(description = "배달팁 참조로 닫을 수 없는 행정동(비어 있지 않으면 저장 시 409)")
    List<ShopDeliveryAreaBlockedResponse> blockedAdminDongs,

    @Schema(description = "좌표·경계 미보유로 판정하지 못한 행정동 수", example = "0")
    int unresolvedCount
) {

    public static ShopDeliveryAreaPolygonPreviewResponse from(
        int maxRadiusMeters,
        boolean withinAllowedRadius,
        List<ShopDeliveryAreaCandidateResponse> adminDongs,
        List<ShopDeliveryAreaCandidateResponse> addedAdminDongs,
        List<ShopDeliveryAreaCandidateResponse> removedAdminDongs,
        List<ShopDeliveryAreaBlockedResponse> blockedAdminDongs,
        int unresolvedCount
    ) {
        return new ShopDeliveryAreaPolygonPreviewResponse(
            maxRadiusMeters,
            withinAllowedRadius,
            adminDongs,
            addedAdminDongs,
            removedAdminDongs,
            blockedAdminDongs,
            unresolvedCount
        );
    }
}
