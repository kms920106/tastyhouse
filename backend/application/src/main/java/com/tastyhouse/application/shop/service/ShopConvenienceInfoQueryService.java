package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopConvenienceInfoQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopConvenienceInfoQueryService implements ShopConvenienceInfoQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopConvenienceInfoQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Optional<ShopConvenienceInfoResult> getConvenienceInfo(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findConvenienceInfo(shopId);
    }

    @Override
    public List<ShopAmenityAssignmentResult> getAmenities(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findAmenityAssignments(shopId);
    }
}
