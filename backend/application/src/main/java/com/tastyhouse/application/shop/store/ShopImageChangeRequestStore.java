package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestStatePort;

public class ShopImageChangeRequestStore implements ShopImageChangeRequestRepository {
    private final ShopImageChangeRequestStatePort shopImageChangeRequestStatePort;

    public ShopImageChangeRequestStore(ShopImageChangeRequestStatePort shopImageChangeRequestStatePort) {
        this.shopImageChangeRequestStatePort = shopImageChangeRequestStatePort;
    }

    @Override
    public ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest) {
        return ShopImageChangeRequestStateMapper.toDomain(shopImageChangeRequestStatePort.save(ShopImageChangeRequestStateMapper.toState(shopImageChangeRequest)));
    }

    @Override
    public Optional<ShopImageChangeRequest> findById(Long id) {
        return shopImageChangeRequestStatePort.findById(id).map(ShopImageChangeRequestStateMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status) {
        return shopImageChangeRequestStatePort.existsByShopIdAndImageTypeAndStatus(
            shopId,
            imageType == null ? null : imageType.name(),
            status == null ? null : status.name()
        );
    }

    @Override
    public boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status) {
        return shopImageChangeRequestStatePort.existsByShopIdAndStatus(shopId, status == null ? null : status.name());
    }
}
