package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;

@CeoApp
public interface ShopPhoneNumberQueryUseCase {

    List<ShopPhoneNumberResult> getPhoneNumbers(Long ceoId, Long shopId);
}
