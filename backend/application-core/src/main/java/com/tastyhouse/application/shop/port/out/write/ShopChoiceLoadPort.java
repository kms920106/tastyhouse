package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopChoice;

public interface ShopChoiceLoadPort {

    Optional<ShopChoice> findById(Long id);
}
