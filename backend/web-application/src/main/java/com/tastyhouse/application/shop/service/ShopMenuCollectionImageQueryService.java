package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMediaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageExposureResult;

@Service
@Transactional(readOnly = true)
class ShopMenuCollectionImageQueryService implements ShopMenuCollectionImageQueryUseCase {

    private final ShopMediaQueryPort shopMediaQueryPort;

    public ShopMenuCollectionImageQueryService(ShopMediaQueryPort shopMediaQueryPort) {
        this.shopMediaQueryPort = shopMediaQueryPort;
    }

    @Override
    public List<ShopMenuCollectionImageExposureResult> getMenuCollectionImages(Long shopId) {
        return shopMediaQueryPort.findMenuCollectionImagesByStatus(shopId, ApprovalStatus.APPROVED.name());
    }
}
