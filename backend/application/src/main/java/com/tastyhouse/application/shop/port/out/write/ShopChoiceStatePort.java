package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopChoiceStatePort {
    Optional<ShopChoiceState> findById(Long id);

    ShopChoiceState save(ShopChoiceState shopChoice);

    void deleteById(Long id);
}
