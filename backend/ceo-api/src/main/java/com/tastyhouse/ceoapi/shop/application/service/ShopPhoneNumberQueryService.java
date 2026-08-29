package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopPhoneNumberQueryUseCase;
import com.tastyhouse.infrastructure.shop.query.ShopPhoneNumberResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopPhoneNumberResponse;

/**
 * 점주용 가게 전화번호 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
public class ShopPhoneNumberQueryService implements ShopPhoneNumberQueryUseCase {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopPhoneNumberQueryService(ShopQueryDao shopQueryDao, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryDao = shopQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopPhoneNumberResponse> getPhoneNumbers(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryDao.findPhoneNumbers(shopId).stream()
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
