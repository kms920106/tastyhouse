package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;

public interface ShopPhoneNumberSavePort {

    ShopPhoneNumber save(ShopPhoneNumber shopPhoneNumber);

    void deleteById(Long id);
}
