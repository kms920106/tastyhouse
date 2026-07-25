package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopPhoneNumber;

public interface ShopPhoneNumberRepository {

    ShopPhoneNumber save(ShopPhoneNumber shopPhoneNumber);

    List<ShopPhoneNumber> findByShopId(Long shopId);

    Optional<ShopPhoneNumber> findById(Long id);

    void deleteById(Long id);
}
