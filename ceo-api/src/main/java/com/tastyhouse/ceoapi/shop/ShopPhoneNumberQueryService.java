package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopPhoneNumberResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.shop.response.ShopPhoneNumberResponse;

/**
 * 점주용 가게 전화번호 조회 서비스(CQRS query 측).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopPhoneNumberQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

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
