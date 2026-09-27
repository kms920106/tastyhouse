package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopConvenienceInfoStatePort {
    Optional<ShopConvenienceInfoState> findByShopId(Long shopId);

    ShopConvenienceInfoState save(ShopConvenienceInfoState shopConvenienceInfo);
}
