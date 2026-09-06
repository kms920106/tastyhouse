package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopChoice;

public interface ShopChoiceRepository {
    Optional<ShopChoice> findById(Long id);

    ShopChoice save(ShopChoice shopChoice);

    void deleteById(Long id);
}
