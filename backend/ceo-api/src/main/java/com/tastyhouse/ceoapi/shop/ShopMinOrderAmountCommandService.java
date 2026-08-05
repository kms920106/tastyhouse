package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;

/**
 * 점주용 가게 최소주문금액 변경 서비스(CQRS command 측).
 *
 * <p>설정 범위(0=미설정, 또는 5,000~30,000원)와 폐업 가드는 도메인 불변식이므로
 * {@link Shop#changeMinOrderAmount(int)}가 담당한다. 이 서비스는 소유권 검증과 트랜잭션 경계,
 * 그리고 변경 후 명시적 저장만 책임진다.
 */
@Service
@Transactional
public class ShopMinOrderAmountCommandService {

    private final ShopRepository shopRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopMinOrderAmountCommandService(ShopRepository shopRepository, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopRepository = shopRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void updateMinOrderAmount(Long ceoId, Long shopId, int minOrderAmount) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shop.changeMinOrderAmount(minOrderAmount);
        shopRepository.save(shop);
    }
}
