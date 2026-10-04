package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageExposureResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;

@Service
@Transactional(readOnly = true)
public class ShopMenuCollectionImageQueryService implements ShopMenuCollectionImageQueryUseCase {

    private final ShopQueryPort shopQueryPort;

    public ShopMenuCollectionImageQueryService(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    @Override
    public List<ShopMenuCollectionImageExposureResult> getMenuCollectionImages(Long shopId) {
        return shopQueryPort.findMenuCollectionImagesByStatus(shopId, ApprovalStatus.APPROVED.name());
    }
}
