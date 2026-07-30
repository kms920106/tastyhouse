package com.tastyhouse.core.domain.shop.domain.service;

import java.math.BigDecimal;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopConvenienceInfo;
import com.tastyhouse.core.domain.shop.domain.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 가게 편의정보(주차·발렛·찾아오는길·표시위치) 불변식(도메인 서비스).
 *
 * <p>찾아오는길 텍스트는 금칙어 검수를 통과해야 하고, 지도 표시 위치는 <b>가게 실제 좌표에서
 * {@value #MAX_DISPLAY_LOCATION_DISTANCE_METERS}m 이내</b>여야 한다(가게 애그리거트의 좌표를 함께 읽어
 * 판정하는 크로스 애그리거트 규칙 — 분류 C). 편의정보는 가게당 1건으로 없으면 생성, 있으면 갱신하는
 * upsert 시맨틱을 가진다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 */
public class ShopConvenienceInfoService {

    private static final double EARTH_RADIUS_METERS = 6371000;
    private static final double MAX_DISPLAY_LOCATION_DISTANCE_METERS = 1000;

    private final ShopConvenienceInfoRepository shopConvenienceInfoRepository;
    private final ShopRepository shopRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;

    public ShopConvenienceInfoService(
        ShopConvenienceInfoRepository shopConvenienceInfoRepository,
        ShopRepository shopRepository,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        this.shopConvenienceInfoRepository = shopConvenienceInfoRepository;
        this.shopRepository = shopRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    /**
     * 편의정보를 upsert 한다. 찾아오는길은 금칙어 검수를, 표시 위치는 가게 좌표 반경 검증을 통과해야 한다.
     */
    public void upsertConvenienceInfo(
        Long shopId,
        Boolean parkingAvailable,
        Boolean parkingPaid,
        Boolean valetAvailable,
        Boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        if (directionsGuide != null) {
            prohibitedWordValidator.validate(directionsGuide);
        }

        if (displayLatitude != null && displayLongitude != null) {
            validateDisplayLocation(shopId, displayLatitude, displayLongitude);
        }

        ShopConvenienceInfo shopConvenienceInfo = shopConvenienceInfoRepository.findByShopId(shopId)
            .map(existing -> {
                existing.update(
                    parkingAvailable,
                    parkingPaid,
                    valetAvailable,
                    valetPaid,
                    directionsGuide,
                    displayLatitude,
                    displayLongitude
                );
                return existing;
            })
            .orElseGet(() -> ShopConvenienceInfo.of(
                shopId,
                parkingAvailable,
                parkingPaid,
                valetAvailable,
                valetPaid,
                directionsGuide,
                displayLatitude,
                displayLongitude
            ));

        shopConvenienceInfoRepository.save(shopConvenienceInfo);
    }

    private void validateDisplayLocation(Long shopId, BigDecimal displayLatitude, BigDecimal displayLongitude) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
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
