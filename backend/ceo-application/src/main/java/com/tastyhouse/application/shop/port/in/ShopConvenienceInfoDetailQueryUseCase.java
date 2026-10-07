package com.tastyhouse.application.shop.port.in;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;

public interface ShopConvenienceInfoDetailQueryUseCase {

    Optional<ShopConvenienceInfoResult> getConvenienceInfo(Long ceoId, Long shopId);
}
