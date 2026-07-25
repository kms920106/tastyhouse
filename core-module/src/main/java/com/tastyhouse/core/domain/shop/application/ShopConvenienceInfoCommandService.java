package com.tastyhouse.core.domain.shop.application;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopConvenienceInfo;
import com.tastyhouse.core.domain.shop.domain.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopConvenienceInfoUpdateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopConvenienceInfoCommandService {

    private static final double EARTH_RADIUS_METERS = 6371000;
    private static final double MAX_DISPLAY_LOCATION_DISTANCE_METERS = 1000;

    private final ShopConvenienceInfoRepository shopConvenienceInfoRepository;
    private final ShopRepository shopRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;

    public void upsertConvenienceInfo(ShopConvenienceInfoUpdateCommand command) {
        if (command.directionsGuide() != null) {
            prohibitedWordValidator.validate(command.directionsGuide());
        }

        if (command.displayLatitude() != null && command.displayLongitude() != null) {
            validateDisplayLocation(command.shopId(), command.displayLatitude(), command.displayLongitude());
        }

        ShopConvenienceInfo shopConvenienceInfo = shopConvenienceInfoRepository.findByShopId(command.shopId())
            .map(existing -> {
                existing.update(
                    command.parkingAvailable(),
                    command.parkingPaid(),
                    command.valetAvailable(),
                    command.valetPaid(),
                    command.directionsGuide(),
                    command.displayLatitude(),
                    command.displayLongitude()
                );
                return existing;
            })
            .orElseGet(() -> ShopConvenienceInfo.of(
                command.shopId(),
                command.parkingAvailable(),
                command.parkingPaid(),
                command.valetAvailable(),
                command.valetPaid(),
                command.directionsGuide(),
                command.displayLatitude(),
                command.displayLongitude()
            ));

        shopConvenienceInfoRepository.save(shopConvenienceInfo);
    }

    private void validateDisplayLocation(Long shopId, BigDecimal displayLatitude, BigDecimal displayLongitude) {
        ShopId id = ShopId.of(shopId);
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        double distanceMeters = distanceMeters(displayLatitude, displayLongitude, shop.getLatitude(), shop.getLongitude());
        if (distanceMeters > MAX_DISPLAY_LOCATION_DISTANCE_METERS) {
            throw new BusinessException(ErrorCode.SHOP_DISPLAY_LOCATION_OUT_OF_RANGE);
        }
    }

    /**
     * 두 좌표 간의 하버사인(Haversine) 거리를 미터 단위로 계산한다.
     */
    private static double distanceMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}
