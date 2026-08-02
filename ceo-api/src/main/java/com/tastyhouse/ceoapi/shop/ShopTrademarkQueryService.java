package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.infrastructure.shop.query.ShopImageChangeRequestResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.file.FileService;
import com.tastyhouse.ceoapi.shop.response.ShopImageChangeRequestItemResponse;
import com.tastyhouse.ceoapi.shop.response.ShopImageStatusResponse;

/**
 * 점주용 가게 상표/대표이미지 상태 조회 서비스(CQRS query 측).
 *
 * <p>현재 적용된 이미지 URL은 소유권 검증이 반환한 도메인 모델에서, 진행 중·과거 변경요청 목록은
 * infra query DAO에서 얻어 함께 조립한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopTrademarkQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final FileService fileService;

    public ShopImageStatusResponse getTrademarkStatus(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        Long trademarkImageFileId = shop.getTrademarkImageFileId() == null ? null : shop.getTrademarkImageFileId().value();
        return toShopImageStatusResponse(resolveImageUrl(trademarkImageFileId), shopId);
    }

    public ShopImageStatusResponse getThumbnailStatus(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        Long thumbnailImageFileId = shop.getThumbnailImageFileId() == null ? null : shop.getThumbnailImageFileId().value();
        return toShopImageStatusResponse(resolveImageUrl(thumbnailImageFileId), shopId);
    }

    private ShopImageStatusResponse toShopImageStatusResponse(String currentImageUrl, Long shopId) {
        List<ShopImageChangeRequestItemResponse> requests = shopQueryDao.findImageChangeRequests(shopId).stream()
            .map(this::toShopImageChangeRequestItemResponse)
            .toList();
        return ShopImageStatusResponse.of(currentImageUrl, requests);
    }

    private ShopImageChangeRequestItemResponse toShopImageChangeRequestItemResponse(ShopImageChangeRequestResult dto) {
        return ShopImageChangeRequestItemResponse.of(
            dto.id(),
            dto.imageType().name(),
            resolveImageUrl(dto.imageFileId()),
            dto.status().name(),
            dto.rejectReason()
        );
    }

    private String resolveImageUrl(Long imageFileId) {
        if (imageFileId == null) {
            return null;
        }
        return fileService.getUrlByFileId(imageFileId);
    }
}
