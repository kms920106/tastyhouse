package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

public interface ShopTemporaryClosureLoadPort {

    List<ShopTemporaryClosure> findByShopId(Long shopId);

    Optional<ShopTemporaryClosure> findById(Long id);
}
