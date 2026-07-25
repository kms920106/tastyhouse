package com.tastyhouse.ceoapi.shop;

import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.core.domain.shop.application.ShopConvenienceInfoCommandService;
import com.tastyhouse.core.domain.shop.application.ShopConvenienceInfoQueryService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopConvenienceInfoUpdateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopConvenienceInfoResult;
import com.tastyhouse.ceoapi.shop.response.ShopAmenityResponse;
import com.tastyhouse.ceoapi.shop.response.ShopConvenienceInfoResponse;

/**
 * 점주용 가게 편의정보·편의시설 관리 중개 서비스. 컨트롤러↔core 위임과 소유권 검증만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class ShopConvenienceInfoService {

    private final ShopQueryService shopQueryService;
    private final ShopCommandService shopCommandService;
    private final ShopConvenienceInfoQueryService shopConvenienceInfoQueryService;
    private final ShopConvenienceInfoCommandService shopConvenienceInfoCommandService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopConvenienceInfoResponse getConvenienceInfo(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopConvenienceInfoQueryService.findConvenienceInfo(shopId)
            .map(this::toShopConvenienceInfoResponse)
            .orElseGet(() -> ShopConvenienceInfoResponse.from(null, shopId, false, false, false, false, null, null, null));
    }

    public void updateConvenienceInfo(
        Long ceoId,
        Long shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopConvenienceInfoUpdateCommand command = ShopConvenienceInfoUpdateCommand.of(
            shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid, directionsGuide, displayLatitude, displayLongitude
        );
        shopConvenienceInfoCommandService.upsertConvenienceInfo(command);
    }

    public List<ShopAmenityResponse> getAmenities(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryService.findShopAmenityAssignments(shopId).stream()
            .map(this::toShopAmenityResponse)
            .toList();
    }

    public Long assignAmenity(Long ceoId, Long shopId, Long amenityCategoryId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopCommandService.assignAmenity(shopId, amenityCategoryId).getId();
    }

    public void unassignAmenity(Long ceoId, Long shopId, Long amenityCategoryId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopCommandService.unassignAmenity(shopId, amenityCategoryId);
    }

    private ShopConvenienceInfoResponse toShopConvenienceInfoResponse(ShopConvenienceInfoResult dto) {
        return ShopConvenienceInfoResponse.from(
            dto.id(),
            dto.shopId(),
            dto.parkingAvailable(),
            dto.parkingPaid(),
            dto.valetAvailable(),
            dto.valetPaid(),
            dto.directionsGuide(),
            dto.displayLatitude(),
            dto.displayLongitude()
        );
    }

    private ShopAmenityResponse toShopAmenityResponse(ShopAmenityAssignmentResult dto) {
        return ShopAmenityResponse.from(
            dto.id(),
            dto.amenityCategoryId(),
            dto.amenity().name(),
            dto.displayName(),
            dto.activeFilePath()
        );
    }
}
