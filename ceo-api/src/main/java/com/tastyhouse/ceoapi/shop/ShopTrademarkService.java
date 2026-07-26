package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.domain.shop.application.ShopImageChangeCommandService;
import com.tastyhouse.core.domain.shop.application.ShopImageChangeQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopImageChangeRequestCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopImageChangeRequestResult;
import com.tastyhouse.ceoapi.file.FileService;
import com.tastyhouse.ceoapi.shop.response.ShopImageChangeRequestItemResponse;
import com.tastyhouse.ceoapi.shop.response.ShopImageStatusResponse;

/**
 * 점주용 가게 상표/대표이미지 변경요청 중개 서비스. 모든 조회·요청은 로그인 점주(ceoId)의
 * 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class ShopTrademarkService {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageChangeCommandService shopImageChangeCommandService;
    private final ShopImageChangeQueryService shopImageChangeQueryService;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileService fileService;
    private final FileQueryService fileQueryService;

    public ShopImageStatusResponse getTrademarkStatus(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toShopImageStatusResponse(resolveImageUrl(shop.getTrademarkImageFileId()), shopId);
    }

    public Long requestTrademarkChange(Long ceoId, Long shopId, MultipartFile file) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateTrademark(file);

        Long imageFileId = fileService.upload(file);
        ShopImageChangeRequestCreateCommand command = ShopImageChangeRequestCreateCommand.of(shopId, ShopImageType.TRADEMARK, imageFileId);
        return shopImageChangeCommandService.requestImageChange(command);
    }

    public ShopImageStatusResponse getThumbnailStatus(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toShopImageStatusResponse(resolveImageUrl(shop.getThumbnailImageFileId()), shopId);
    }

    public Long requestThumbnailChange(Long ceoId, Long shopId, MultipartFile file) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateContentImage(file, false);

        Long imageFileId = fileService.upload(file);
        ShopImageChangeRequestCreateCommand command = ShopImageChangeRequestCreateCommand.of(shopId, ShopImageType.THUMBNAIL, imageFileId);
        return shopImageChangeCommandService.requestImageChange(command);
    }

    private ShopImageStatusResponse toShopImageStatusResponse(String currentImageUrl, Long shopId) {
        List<ShopImageChangeRequestItemResponse> requests = shopImageChangeQueryService.findByShopId(shopId).stream()
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
        return fileQueryService.findFilePath(UploadedFileId.of(imageFileId))
            .map(fileService::getUrlByPath)
            .orElse(null);
    }
}
