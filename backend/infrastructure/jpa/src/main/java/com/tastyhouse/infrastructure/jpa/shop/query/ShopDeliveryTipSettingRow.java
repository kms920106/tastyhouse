package com.tastyhouse.infrastructure.jpa.shop.query;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;

public record ShopDeliveryTipSettingRow(Long shopId, ShopDeliveryTipSettingResult setting) {
}
