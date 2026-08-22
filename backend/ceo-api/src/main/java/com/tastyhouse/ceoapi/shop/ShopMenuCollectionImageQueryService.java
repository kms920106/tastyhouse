package com.tastyhouse.ceoapi.shop;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.shop.query.ShopMenuCollectionImageResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.shop.response.ShopMenuCollectionImageResponse;

/**
 * 점주용 메뉴모음컷 조회 서비스(CQRS query 측).
 *
 * <p>검수 대기·반려 건까지 함께 내려보낸다 — 점주 화면이 진행 상태를 보여주어야 한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopMenuCollectionImageQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopMenuCollectionImageQueryService(
        ShopQueryDao shopQueryDao,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopQueryDao = shopQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public List<ShopMenuCollectionImageResponse> getMenuCollectionImages(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopQueryDao.findMenuCollectionImages(shopId).stream()
            .map(this::toShopMenuCollectionImageResponse)
            .toList();
    }

    private ShopMenuCollectionImageResponse toShopMenuCollectionImageResponse(ShopMenuCollectionImageResult dto) {
        return ShopMenuCollectionImageResponse.from(
            dto.id(),
            dto.imageUrl(),
            dto.sort(),
            dto.status().name(),
            dto.rejectReason()
        );
    }
}
