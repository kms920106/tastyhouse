package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopOriginInfoService;

/**
 * 점주용 가게 원산지 표시 변경 서비스(CQRS command 측).
 *
 * <p>입력 방식별 필수 필드·형식 검증과 상호 배타 정리, 변경이력({@code ORIGIN_INFO}) 기록은 도메인
 * 서비스 {@link ShopOriginInfoService}가 담당하고, 여기서는 소유권 검증과 트랜잭션 경계, 변경 주체
 * 전달, 입력 방식 문자열의 enum 승격만 책임진다.
 */
@Service
@Transactional
public class ShopOriginInfoCommandService {

    private final ShopOriginInfoService shopOriginInfoService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOriginInfoCommandService(
        ShopOriginInfoService shopOriginInfoService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopOriginInfoService = shopOriginInfoService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void updateOriginInfo(Long ceoId, Long shopId, String sourceType, String content, String url) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopOriginInfoService.upsertOriginInfo(
            shopId,
            OriginSourceType.from(sourceType),
            content,
            url,
            ShopChangeActor.ceo(ceoId)
        );
    }
}
