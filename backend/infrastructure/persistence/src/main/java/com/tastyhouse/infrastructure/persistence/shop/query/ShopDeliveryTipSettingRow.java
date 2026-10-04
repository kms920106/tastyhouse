package com.tastyhouse.infrastructure.persistence.shop.query;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;

public record ShopDeliveryTipSettingRow(Long shopId, ShopDeliveryTipSettingResult setting) {
}
