package com.tastyhouse.application.shop.port.out;

import java.util.List;

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
