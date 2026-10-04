package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;

public interface ShopHygieneBadgeManagementQueryUseCase {

    List<ShopHygieneBadgeResult> getHygieneBadges(Long shopId);
}
