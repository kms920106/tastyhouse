package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopPhoneNumberQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.ceoapplication.shop.response.ShopPhoneNumberResponse;

/**
 * 점주용 가게 전화번호 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
public class ShopPhoneNumberQueryService implements ShopPhoneNumberQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopPhoneNumberQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopPhoneNumberResponse> getPhoneNumbers(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findPhoneNumbers(shopId).stream()
            .map(this::toShopPhoneNumberResponse)
            .toList();
    }

    private ShopPhoneNumberResponse toShopPhoneNumberResponse(ShopPhoneNumberResult dto) {
        return ShopPhoneNumberResponse.from(
            dto.id(),
            dto.phoneNumber(),
            dto.primary(),
            dto.virtual()
        );
    }
}
