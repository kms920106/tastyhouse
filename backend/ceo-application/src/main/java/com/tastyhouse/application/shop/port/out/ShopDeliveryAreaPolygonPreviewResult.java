package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 배달지역 도형 미리보기 결과 — 저장하지 않고 "저장하면 무엇이 열리고 무엇이 닫히는지"만 계산한 것.
 *
 * <p><b>챕터 09</b>에서 신설. 환산은 도메인의 {@code DeliveryAreaProjection}이 수행하고 열림·닫힘·차단
 * 분류는 등록 현황과의 대조로 결정되므로 전부 application의 일이다. 표현 계약은 이 결과를 옮기기만 한다.
 */
public record ShopDeliveryAreaPolygonPreviewResult(
    int maxRadiusMeters,
    boolean withinAllowedRadius,
    List<ShopDeliveryAreaCandidateView> adminDongs,
    List<ShopDeliveryAreaCandidateView> addedAdminDongs,
    List<ShopDeliveryAreaCandidateView> removedAdminDongs,
    List<ShopDeliveryAreaBlockedView> blockedAdminDongs,
    int unresolvedCount
) {
}
