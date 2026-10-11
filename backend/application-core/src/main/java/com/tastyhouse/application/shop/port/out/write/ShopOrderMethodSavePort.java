package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;

public interface ShopOrderMethodSavePort {

    ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod);

    void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod);
}
