package com.tastyhouse.ceoapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.application.dto.ShopSearchCondition;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.ceoapi.common.PaginationResponse;
import com.tastyhouse.ceoapi.file.FileService;
import com.tastyhouse.ceoapi.shop.response.ShopDetailResponse;
import com.tastyhouse.ceoapi.shop.response.ShopListItemResponse;

/**
 * 점주용 가게 관리 중개 서비스. 컨트롤러↔core 위임과 request→command / result→response 변환만 담당한다.
 * 모든 조회·수정은 로그인 점주(ceoId)의 소유 가게로 한정한다.
 */
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopQueryService shopQueryService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final FileService fileService;
    private final FileQueryService fileQueryService;

    public PaginationResponse<ShopListItemResponse> getMyShops(
        Long ceoId,
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed, ceoId);
        PageResult<ShopListItemResponse> pageResult = shopQueryService.findShops(condition, page, size)
            .map(this::toShopListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public ShopDetailResponse getMyShop(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toShopDetailResponse(shop);
    }

    private ShopListItemResponse toShopListItemResponse(ShopListItemResult dto) {
        return ShopListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.roadAddress(),
            dto.rating(),
            dto.permanentlyClosed()
        );
    }

    private ShopDetailResponse toShopDetailResponse(Shop shop) {
        return ShopDetailResponse.from(
            shop.getId(),
            shop.getStationId(),
            shop.getName(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getRating(),
            shop.getRoadAddress(),
            shop.getLotAddress(),
            shop.getPhoneNumber(),
            resolveImageUrl(shop.getThumbnailImageFileId()),
            resolveImageUrl(shop.getTrademarkImageFileId()),
            shop.isPermanentlyClosed(),
            shop.isHidden(),
            shop.isClosedOnPublicHolidays()
        );
    }

    private String resolveImageUrl(Long imageFileId) {
        if (imageFileId == null) {
            return null;
        }
        return fileQueryService.findFilePath(UploadedFileId.of(imageFileId))
            .map(fileService::getUrlByPath)
            .orElse(null);
    }
}
