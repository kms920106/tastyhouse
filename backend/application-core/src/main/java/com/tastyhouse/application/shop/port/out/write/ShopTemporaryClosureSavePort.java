package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

public interface ShopTemporaryClosureSavePort {

    ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure);

    void deleteById(Long id);
}
