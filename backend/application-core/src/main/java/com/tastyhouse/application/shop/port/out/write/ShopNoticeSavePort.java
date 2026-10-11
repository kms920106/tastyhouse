package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopNotice;

public interface ShopNoticeSavePort {

    ShopNotice save(ShopNotice shopNotice);

    void deleteById(Long id);
}
