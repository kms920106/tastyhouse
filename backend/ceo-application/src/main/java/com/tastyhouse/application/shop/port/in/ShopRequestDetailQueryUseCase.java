package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopRequestDetailViewResult;

public interface ShopRequestDetailQueryUseCase {

    ShopRequestDetailViewResult getRequestDetail(Long ceoId, Long shopId, Long requestId);
}
