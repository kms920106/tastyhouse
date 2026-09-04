package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaAdjustmentOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentQueryPort;

/**
 * 점주용 배달지역 조정 신청 이력 조회 서비스(CQRS query 측).
 *
 * <p>동의서 URL은 infra query DAO가 조인으로 완성하므로 이 서비스는 소유권 검증과 Result 언패킹만
 * 담당한다. write 포트는 주입하지 않는다(CQRS 교차 주입 금지).
 */
@Service
@Transactional(readOnly = true)
public class ShopDeliveryAreaAdjustmentOwnerQueryService implements ShopDeliveryAreaAdjustmentOwnerQueryUseCase {

    private final ShopDeliveryAreaAdjustmentQueryPort shopDeliveryAreaAdjustmentQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaAdjustmentOwnerQueryService(
        ShopDeliveryAreaAdjustmentQueryPort shopDeliveryAreaAdjustmentQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaAdjustmentQueryPort = shopDeliveryAreaAdjustmentQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopDeliveryAreaAdjustmentQueryPort.findAdjustmentRequests(shopId);
    }

}
