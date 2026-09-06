package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ShopImageChangeRequestRepository {
    ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest);

    Optional<ShopImageChangeRequest> findById(Long id);

    boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status);

    boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status);
}
