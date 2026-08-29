package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopHygieneBadgeQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.apicommon.shop.response.ShopHygieneBadgeResponse;

/**
 * 점주용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측).
 *
 * <p>등록/삭제는 admin 전용이라 ceo-api에는 command 서비스를 두지 않는다. 모든 조회는 로그인
 * 점주(ceoId)의 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopHygieneBadgeQueryService implements ShopHygieneBadgeQueryUseCase {

    private final ShopQueryPort shopQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopHygieneBadgeQueryService(ShopQueryPort shopQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryPort = shopQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopHygieneBadgeResponse> getHygieneBadges(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryPort.findHygieneBadges(shopId).stream()
            .map(this::toShopHygieneBadgeResponse)
            .toList();
    }

    private ShopHygieneBadgeResponse toShopHygieneBadgeResponse(ShopHygieneBadgeResult dto) {
        return ShopHygieneBadgeResponse.of(
            dto.id(),
            dto.shopId(),
            dto.badgeType().name(),
            dto.certifiedDate(),
            dto.lastInspectionMonth()
        );
    }
}
