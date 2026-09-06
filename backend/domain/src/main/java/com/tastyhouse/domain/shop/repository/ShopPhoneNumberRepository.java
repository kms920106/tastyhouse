package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;

public interface ShopPhoneNumberRepository {

    ShopPhoneNumber save(ShopPhoneNumber shopPhoneNumber);

    List<ShopPhoneNumber> findByShopId(Long shopId);

    Optional<ShopPhoneNumber> findById(Long id);

    void deleteById(Long id);
}
