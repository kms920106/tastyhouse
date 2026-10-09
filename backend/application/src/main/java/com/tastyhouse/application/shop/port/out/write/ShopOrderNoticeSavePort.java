package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;

public interface ShopOrderNoticeSavePort {

    ShopOrderNotice save(ShopOrderNotice shopOrderNotice);
}
