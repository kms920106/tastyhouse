package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;

/**
 * 점주용 가게 배달가능지역 조회 서비스(CQRS query 측).
 *
 * <p>행정동 이름 조립은 infra query DAO가 조인으로 완성하므로 이 서비스는 소유권 검증과 Result 언패킹만
 * 담당한다. write 포트는 주입하지 않는다(CQRS 교차 주입 금지).
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopDeliveryAreaQueryService implements ShopDeliveryAreaQueryUseCase {

    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaQueryService(ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopDeliveryAreaItemResult> getDeliveryAreas(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopDeliveryAreaQueryPort.findDeliveryAreas(shopId);
    }

}
