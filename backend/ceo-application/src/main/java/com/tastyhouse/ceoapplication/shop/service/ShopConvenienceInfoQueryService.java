package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopConvenienceInfoQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.ceoapplication.shop.response.ShopAmenityResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopConvenienceInfoResponse;

/**
 * 점주용 가게 편의정보·편의시설 조회 서비스(CQRS query 측).
 *
 * <p>편의정보가 아직 등록되지 않은 가게는 빈 기본값 응답을 돌려준다(기존 동작 유지).
 */
@Service
@Transactional(readOnly = true)
public class ShopConvenienceInfoQueryService implements ShopConvenienceInfoQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopConvenienceInfoQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopConvenienceInfoResponse getConvenienceInfo(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findConvenienceInfo(shopId)
            .map(this::toShopConvenienceInfoResponse)
            .orElseGet(() -> ShopConvenienceInfoResponse.from(null, shopId, false, false, false, false, null, null, null));
    }

    @Override
    public List<ShopAmenityResponse> getAmenities(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findAmenityAssignments(shopId).stream()
            .map(this::toShopAmenityResponse)
            .toList();
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
            dto.activeIconUrl()
        );
    }
}
