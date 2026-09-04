package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopHygieneBadgeOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

/**
 * 점주용 가게 위생 인증 뱃지 조회 서비스(CQRS query 측).
 *
 * <p>등록/삭제는 admin 전용이라 ceo-api에는 command 서비스를 두지 않는다. 모든 조회는 로그인
 * 점주(ceoId)의 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopHygieneBadgeOwnerQueryService implements ShopHygieneBadgeOwnerQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopHygieneBadgeOwnerQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopHygieneBadgeResult> getHygieneBadges(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findHygieneBadges(shopId);
    }
}
