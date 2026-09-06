package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;

@CeoApp
public interface ShopHygieneBadgeOwnerQueryUseCase {

    List<ShopHygieneBadgeResult> getHygieneBadges(Long ceoId, Long shopId);
}
