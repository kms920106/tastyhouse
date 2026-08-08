package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;

/**
 * 점주용 가게 예약주문 운영 여부 변경 서비스(CQRS command 측).
 *
 * <p>폐업 가드와 "OFF는 신규 예약만 차단한다"는 규칙은 도메인 불변식이므로
 * {@link Shop#changeScheduledOrderEnabled(boolean)}가 담당한다. 이 서비스는 소유권 검증과 트랜잭션 경계,
 * 그리고 변경 후 명시적 저장만 책임진다({@link ShopMinOrderAmountCommandService}와 동일한 형태).
 */
@Service
@Transactional
public class ShopScheduledOrderCommandService {

    private final ShopRepository shopRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopScheduledOrderCommandService(
        ShopRepository shopRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRepository = shopRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void updateScheduledOrder(Long ceoId, Long shopId, boolean enabled) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shop.changeScheduledOrderEnabled(enabled);
        shopRepository.save(shop);
    }
}
