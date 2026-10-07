package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

public interface ShopOrderMethodListQueryUseCase {

    List<ShopOrderMethodResult> getOrderMethods(Long ceoId, Long shopId);
}
