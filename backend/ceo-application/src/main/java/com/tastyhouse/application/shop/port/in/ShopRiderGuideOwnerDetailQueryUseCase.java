package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;

public interface ShopRiderGuideOwnerDetailQueryUseCase {

    ShopRiderGuideResult getRiderGuide(Long ceoId, Long shopId);
}
