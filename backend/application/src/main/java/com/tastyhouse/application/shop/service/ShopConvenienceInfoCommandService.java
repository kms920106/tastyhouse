package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopConvenienceInfoService;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopConvenienceInfoCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopConvenienceInfoUpdateCommand;

/**
 * 점주용 가게 편의정보·편의시설 변경 서비스(CQRS command 측).
 *
 * <p>찾아오는길 금칙어 검수·표시 위치 반경(1km) 검증 불변식과 변경이력({@code CONVENIENCE_INFO}·
 * {@code AMENITY}) 기록은 도메인 서비스 {@link ShopConvenienceInfoService}가 담당하고, 여기서는
 * 소유권 검증과 트랜잭션 경계, 변경 주체 전달만 책임진다.
 */
@Service
@CeoApp
@Transactional
public class ShopConvenienceInfoCommandService implements ShopConvenienceInfoCommandUseCase {

    private final ShopConvenienceInfoService shopConvenienceInfoService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopConvenienceInfoCommandService(
        ShopConvenienceInfoService shopConvenienceInfoService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopConvenienceInfoService = shopConvenienceInfoService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateConvenienceInfo(ShopConvenienceInfoUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        boolean parkingAvailable = command.parkingAvailable();
        boolean parkingPaid = command.parkingPaid();
        boolean valetAvailable = command.valetAvailable();
        boolean valetPaid = command.valetPaid();
        String directionsGuide = command.directionsGuide();
        BigDecimal displayLatitude = command.displayLatitude();
        BigDecimal displayLongitude = command.displayLongitude();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopConvenienceInfoService.upsertConvenienceInfo(
            shopId,
            parkingAvailable,
            parkingPaid,
            valetAvailable,
            valetPaid,
            directionsGuide,
            displayLatitude,
            displayLongitude,
            ShopChangeActor.ceo(ceoId)
        );
    }

    @Override
    public Long assignAmenity(ShopAmenityOwnerAssignCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopConvenienceInfoService.assignAmenity(shopId, amenityCategoryId, ShopChangeActor.ceo(ceoId));
    }

    @Override
    public void unassignAmenity(ShopAmenityOwnerUnassignCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopConvenienceInfoService.unassignAmenity(shopId, amenityCategoryId, ShopChangeActor.ceo(ceoId));
    }
}
