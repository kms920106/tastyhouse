package com.tastyhouse.ceoapi.shop;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.domain.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.shop.domain.service.ShopConvenienceInfoService;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주용 가게 편의정보·편의시설 변경 서비스(CQRS command 측).
 *
 * <p>찾아오는길 금칙어 검수와 표시 위치 반경(1km) 검증 불변식은 도메인 서비스
 * {@link ShopConvenienceInfoService}가 담당한다. 편의시설 배정/해제는 단일 애그리거트 연산이라
 * write 포트로 직접 다룬다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopConvenienceInfoCommandService {

    private final ShopConvenienceInfoService shopConvenienceInfoService;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

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
        shopConvenienceInfoService.upsertConvenienceInfo(
            shopId,
            parkingAvailable,
            parkingPaid,
            valetAvailable,
            valetPaid,
            directionsGuide,
            displayLatitude,
            displayLongitude
        );
    }

    public Long assignAmenity(Long ceoId, Long shopId, Long amenityCategoryId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopDetailRepository.findAmenityCategoryById(amenityCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));
        ShopAmenity amenity = shopDetailRepository.saveAmenity(ShopAmenity.of(ShopId.of(shopId), ShopAmenityCategoryId.of(amenityCategoryId)));
        return amenity.getId();
    }

    public void unassignAmenity(Long ceoId, Long shopId, Long amenityCategoryId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopDetailRepository.deleteAmenityByShopIdAndCategoryId(shopId, amenityCategoryId);
    }
}
