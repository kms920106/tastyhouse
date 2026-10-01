package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;

public interface ShopImageChangeRequestPersistencePort {

    ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest);

    Optional<ShopImageChangeRequest> findById(Long id);

    boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status);

    boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status);
}
