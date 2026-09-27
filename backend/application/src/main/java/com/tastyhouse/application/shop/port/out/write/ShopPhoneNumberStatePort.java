package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ShopPhoneNumberStatePort {
    ShopPhoneNumberState save(ShopPhoneNumberState shopPhoneNumber);

    List<ShopPhoneNumberState> findByShopId(Long shopId);

    Optional<ShopPhoneNumberState> findById(Long id);

    void deleteById(Long id);
}
