package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;

public interface ShopOwnerMessageHistoryLoadPort {

    Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId);
}
