package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;

public interface ShopPhoneNumberQueryUseCase {

    List<ShopPhoneNumberResult> getPhoneNumbers(Long ceoId, Long shopId);
}
