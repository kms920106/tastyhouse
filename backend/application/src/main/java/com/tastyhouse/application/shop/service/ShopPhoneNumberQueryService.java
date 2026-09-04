package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhoneNumberQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

/**
 * 점주용 가게 전화번호 조회 서비스(CQRS query 측).
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopPhoneNumberQueryService implements ShopPhoneNumberQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopPhoneNumberQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopPhoneNumberResult> getPhoneNumbers(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findPhoneNumbers(shopId);
    }

}
