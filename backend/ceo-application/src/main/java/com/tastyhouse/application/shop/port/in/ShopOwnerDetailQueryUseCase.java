package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopOwnerDetailViewResult;

public interface ShopOwnerDetailQueryUseCase {

    ShopOwnerDetailViewResult getMyShop(Long ceoId, Long shopId);
}
