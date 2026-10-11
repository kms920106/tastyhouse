package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;

public interface ShopPhoneNumberLoadPort {

    List<ShopPhoneNumber> findByShopId(Long shopId);

    Optional<ShopPhoneNumber> findById(Long id);
}
