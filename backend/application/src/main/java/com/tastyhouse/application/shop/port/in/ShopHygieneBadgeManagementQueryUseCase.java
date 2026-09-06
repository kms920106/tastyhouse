package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;

@AdminApp
public interface ShopHygieneBadgeManagementQueryUseCase {

    List<ShopHygieneBadgeResult> getHygieneBadges(Long shopId);
}
