package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipRegionSpec;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipScheduleSpec;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipTierSpec;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipCommandUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipDistanceRemoveCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipDistanceUpdateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipHolidayUpdateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipRegionsRemoveCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipRegionsUpdateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipSchedulesUpdateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryTipTiersUpdateCommand;

/**
 * 점주용 가게 배달팁 변경 서비스(CQRS command 측).
 *
 * <p>구간 개수·단조성, 거리별↔지역별 상호 배타, 행정동의 배달가능지역 포함 여부, 시간대 겹침 같은
 * 불변식은 전부 도메인 서비스 {@link ShopDeliveryTipService}가 담당한다. 이 서비스는 소유권 검증과
 * 트랜잭션 경계, 식별자 VO·도메인 enum 승격, Command → Spec 조립만 책임진다
 * ({@code ShopMinOrderAmountCommandService}와 동일 구조).
 *
 * <p><b>여기서 추가로 {@code save}를 호출하지 않는다</b> — {@link ShopDeliveryTipService}의 각
 * 메서드가 내부에서 {@code ShopDeliveryTipRepository}에 직접 저장까지 마치기 때문이다(컬렉션
 * replace-all은 삭제와 저장이 한 연산이라 저장 책임을 도메인 서비스가 갖는다).
 *
 * <p><b>변경이력</b>: 배달팁 5종 기록은 replace-all을 위해 컬렉션을 삭제 전에 읽을 수 있는
 * {@link ShopDeliveryTipService}가 담당하고, 이 서비스는 변경 주체({@link ShopChangeActor})만 만들어
 * 전달한다({@code ShopStatusCommandService}와 동일한 형태).
 *
 * <p>{@code dayType}·{@code surchargeUnit}은 HTTP 경계에서 {@code String}으로 받고 여기서
 * {@link DayType#from(String)}·{@link DeliveryTipDistanceUnit#from(String)}으로 승격한다.
 */
@Service
@Transactional
public class ShopDeliveryTipCommandService implements ShopDeliveryTipCommandUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipCommandService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipService = shopDeliveryTipService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateTiers(ShopDeliveryTipTiersUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        List<ShopDeliveryTipTierSpec> specs = command.tiers().stream()
            .map(tier -> ShopDeliveryTipTierSpec.of(tier.minOrderAmount(), tier.tipAmount()))
            .toList();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.replaceTiers(targetShopId, specs, actor);
    }

    @Override
    public void updateDistanceTip(ShopDeliveryTipDistanceUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Integer baseDistanceMeters = command.baseDistanceMeters();
        String surchargeUnit = command.surchargeUnit();
        Integer surchargeAmount = command.surchargeAmount();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        DeliveryTipDistanceUnit unit = DeliveryTipDistanceUnit.from(surchargeUnit);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.changeDistanceTip(targetShopId, baseDistanceMeters, unit, surchargeAmount, actor);
    }

    @Override
    public void removeDistanceTip(ShopDeliveryTipDistanceRemoveCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.clearDistanceTip(targetShopId, actor);
    }

    @Override
    public void updateRegionTips(ShopDeliveryTipRegionsUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        List<ShopDeliveryTipRegionSpec> specs = command.regions().stream()
            .map(region -> ShopDeliveryTipRegionSpec.of(region.adminDongId(), region.tipAmount()))
            .toList();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.replaceRegionTips(targetShopId, specs, actor);
    }

    @Override
    public void removeRegionTips(ShopDeliveryTipRegionsRemoveCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.clearRegionTips(targetShopId, actor);
    }

    @Override
    public void updateScheduleTips(ShopDeliveryTipSchedulesUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        List<ShopDeliveryTipScheduleSpec> specs = command.schedules().stream()
            .map(schedule -> ShopDeliveryTipScheduleSpec.of(
                DayType.from(schedule.dayType()),
                schedule.startTime(),
                schedule.endTime(),
                schedule.tipAmount()
            ))
            .toList();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.replaceScheduleTips(targetShopId, specs, actor);
    }

    @Override
    public void updateHolidayTip(ShopDeliveryTipHolidayUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        int tipAmount = command.tipAmount();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.changeHolidayTip(targetShopId, tipAmount, actor);
    }
}
