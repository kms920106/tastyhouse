package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryAreaAdjustmentListItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryAreaAdjustmentQueryDao;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaAdjustmentItemResponse;

/**
 * 점주용 배달지역 조정 신청 이력 조회 서비스(CQRS query 측).
 *
 * <p>동의서 URL은 infra query DAO가 조인으로 완성하므로 이 서비스는 소유권 검증과 Result 언패킹만
 * 담당한다. write 포트는 주입하지 않는다(CQRS 교차 주입 금지).
 */
@Service
@Transactional(readOnly = true)
public class ShopDeliveryAreaAdjustmentQueryService {

    private final ShopDeliveryAreaAdjustmentQueryDao shopDeliveryAreaAdjustmentQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaAdjustmentQueryService(
        ShopDeliveryAreaAdjustmentQueryDao shopDeliveryAreaAdjustmentQueryDao,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaAdjustmentQueryDao = shopDeliveryAreaAdjustmentQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public List<ShopDeliveryAreaAdjustmentItemResponse> getAdjustmentRequests(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopDeliveryAreaAdjustmentQueryDao.findAdjustmentRequests(shopId).stream()
            .map(this::toShopDeliveryAreaAdjustmentItemResponse)
            .toList();
    }

    private ShopDeliveryAreaAdjustmentItemResponse toShopDeliveryAreaAdjustmentItemResponse(ShopDeliveryAreaAdjustmentListItemResult dto) {
        return ShopDeliveryAreaAdjustmentItemResponse.from(
            dto.id(),
            dto.counterpartShopName(),
            dto.counterpartBusinessNumber(),
            dto.franchiseName(),
            dto.reason(),
            dto.consentFileUrl(),
            dto.status().name(),
            dto.rejectReason(),
            dto.createdAt()
        );
    }
}
