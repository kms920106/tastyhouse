package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopChoice;

public interface ShopChoiceSavePort {

    ShopChoice save(ShopChoice shopChoice);

    void deleteById(Long id);
}
