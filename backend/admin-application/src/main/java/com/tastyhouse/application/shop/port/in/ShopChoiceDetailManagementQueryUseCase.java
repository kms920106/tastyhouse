package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopChoiceDetailResult;

public interface ShopChoiceDetailManagementQueryUseCase {

    ShopChoiceDetailResult getShopChoice(Long id);
}
