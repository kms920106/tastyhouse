package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopOrderMethodItemResult;

public interface ShopOrderMethodQueryUseCase {

    List<ShopOrderMethodItemResult> getShopOrderMethods(Long shopId);
}
