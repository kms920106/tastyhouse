package com.tastyhouse.ceoapi.shop;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopDeliveryAreaItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryAreaQueryDao;
import com.tastyhouse.ceoapi.shop.response.ShopDeliveryAreaItemResponse;

/**
 * 점주용 가게 배달가능지역 조회 서비스(CQRS query 측).
 *
 * <p>행정동 이름 조립은 infra query DAO가 조인으로 완성하므로 이 서비스는 소유권 검증과 Result 언패킹만
 * 담당한다. write 포트는 주입하지 않는다(CQRS 교차 주입 금지).
 */
@Service
@Transactional(readOnly = true)
public class ShopDeliveryAreaQueryService {

    private final ShopDeliveryAreaQueryDao shopDeliveryAreaQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaQueryService(ShopDeliveryAreaQueryDao shopDeliveryAreaQueryDao, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryAreaQueryDao = shopDeliveryAreaQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public List<ShopDeliveryAreaItemResponse> getDeliveryAreas(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopDeliveryAreaQueryDao.findDeliveryAreas(shopId).stream()
            .map(this::toShopDeliveryAreaItemResponse)
            .toList();
    }

    private ShopDeliveryAreaItemResponse toShopDeliveryAreaItemResponse(ShopDeliveryAreaItemResult dto) {
        return ShopDeliveryAreaItemResponse.from(
            dto.id(),
            dto.adminDongId(),
            dto.regionName(),
            dto.source()
        );
    }
}
