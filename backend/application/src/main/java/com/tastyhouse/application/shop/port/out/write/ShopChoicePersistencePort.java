package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopChoice;

public interface ShopChoicePersistencePort {

    Optional<ShopChoice> findById(Long id);

    ShopChoice save(ShopChoice shopChoice);

    void deleteById(Long id);
}
