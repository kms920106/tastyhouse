package com.tastyhouse.ceoapi.shop;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        List<ShopImageChangeRequestResult> changeRequests = shopQueryDao.findImageChangeRequests(shopId);

        // 변경요청마다 이미지 URL을 단건 조회하면 요청 수만큼 쿼리가 나가므로(N+1), 파일 식별자를 모아
        // 한 번에 변환한 뒤 매핑한다. 현재 적용 이미지는 단건이라 그대로 단건 변환을 쓴다.
        Map<Long, String> imageUrls = fileService.getUrlsByFileIds(
            changeRequests.stream()
                .map(ShopImageChangeRequestResult::imageFileId)
                .filter(Objects::nonNull)
                .toList()
        );

        List<ShopImageChangeRequestItemResponse> requests = changeRequests.stream()
            .map(dto -> toShopImageChangeRequestItemResponse(dto, imageUrls))
            .toList();
        return ShopImageStatusResponse.of(currentImageUrl, requests);
    }

    private ShopImageChangeRequestItemResponse toShopImageChangeRequestItemResponse(
        ShopImageChangeRequestResult dto,
        Map<Long, String> imageUrls
    ) {
        return ShopImageChangeRequestItemResponse.of(
            dto.id(),
            dto.imageType().name(),
            dto.imageFileId() == null ? null : imageUrls.get(dto.imageFileId()),
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
