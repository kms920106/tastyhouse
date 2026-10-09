package com.tastyhouse.application.shop.port.out.write;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopOrderMethod;

public interface ShopOrderMethodLoadPort {

    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);
}
