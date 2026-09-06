package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.service.ShopBusinessHourService;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopBusinessHourCommandService implements ShopBusinessHourCommandUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBusinessHourCommandService(
        ShopBusinessHourService shopBusinessHourService,
        ShopDetailRepository shopDetailRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopDetailRepository = shopDetailRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long createBusinessHour(ShopBusinessHourOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopBusinessHour businessHour = shopBusinessHourService.createBusinessHour(
            shopId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
        return businessHour.getId();
    }

    @Override
    public void updateBusinessHour(ShopBusinessHourOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long businessHourId = command.businessHourId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        validateBusinessHourOwnership(ceoId, businessHourId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.updateBusinessHour(
            businessHourId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
    }

    @Override
    public void deleteBusinessHour(ShopBusinessHourOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long businessHourId = command.businessHourId();

        validateBusinessHourOwnership(ceoId, businessHourId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.deleteBusinessHour(businessHourId, actor);
    }

    @Override
    public Long createBreakTime(ShopBreakTimeOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopBreakTime breakTime = shopBusinessHourService.createBreakTime(
            shopId, DayType.from(dayType), startTime, endTime, actor
        );
        return breakTime.getId();
    }

    @Override
    public void updateBreakTime(ShopBreakTimeOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long breakTimeId = command.breakTimeId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        validateBreakTimeOwnership(ceoId, breakTimeId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime, actor);
    }

    @Override
    public void deleteBreakTime(ShopBreakTimeOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long breakTimeId = command.breakTimeId();

        validateBreakTimeOwnership(ceoId, breakTimeId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.deleteBreakTime(breakTimeId, actor);
    }

    private void validateBusinessHourOwnership(Long ceoId, Long businessHourId) {
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(businessHourId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, businessHour.getShopId().value());
    }

    private void validateBreakTimeOwnership(Long ceoId, Long breakTimeId) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(breakTimeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, breakTime.getShopId().value());
    }
}
