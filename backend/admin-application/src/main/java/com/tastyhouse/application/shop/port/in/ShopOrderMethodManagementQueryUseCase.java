package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

public interface ShopOrderMethodManagementQueryUseCase {

    List<ShopOrderMethodResult> getOrderMethods(Long id);
}
