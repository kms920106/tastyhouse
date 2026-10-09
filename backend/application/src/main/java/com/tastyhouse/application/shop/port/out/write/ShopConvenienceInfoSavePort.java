package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;

public interface ShopConvenienceInfoSavePort {

    ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo);
}
