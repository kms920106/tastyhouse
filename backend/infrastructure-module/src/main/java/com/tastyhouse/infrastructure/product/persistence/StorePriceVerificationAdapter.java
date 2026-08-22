package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.port.StorePriceVerificationPort;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 단위 매장가격 인증 ON/OFF 플래그 포트({@link StorePriceVerificationPort}) 어댑터.
 *
 * <p><b>왜 포트를 경유하는가</b>: 인증 요청 애그리거트는 product 컨텍스트가 소유하지만(승인의 본체가
 * {@code PRODUCT_PRICE}를 채우는 일이므로), 인증 ON/OFF는 가게 단위 상태라 {@code SHOP}에 있다.
 * product의 도메인 서비스가 {@code shop.model}·{@code shop.repository}를 직접 import하면 컨텍스트 경계
 * 규칙({@code ContextBoundaryTest})을 위반하므로, 이 플래그만 좁은 포트로 뽑고 어댑터가 shop 쪽으로
 * 위임한다.
 *
 * <p>{@code ProductReviewStatisticsAdapter}와 같은 형태로 {@code @Component}다 — 도메인 write 포트
 * 구현({@code @Repository})이 아니라 출력 포트 어댑터이기 때문이다.
 */
@Component
public class StorePriceVerificationAdapter implements StorePriceVerificationPort {

    private final ShopRepository shopRepository;

    public StorePriceVerificationAdapter(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public boolean isStorePriceVerified(Long shopId) {
        return loadShop(shopId).isStorePriceVerified();
    }

    @Override
    public void verifyStorePrice(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.verifyStorePrice();
        shopRepository.save(shop);
    }

    @Override
    public void clearStorePriceVerification(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.clearStorePriceVerification();
        shopRepository.save(shop);
    }

    private Shop loadShop(Long shopId) {
        return shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }
}
