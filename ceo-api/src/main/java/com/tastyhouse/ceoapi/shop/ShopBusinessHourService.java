package com.tastyhouse.ceoapi.shop;

import java.time.LocalTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.DayType;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopBreakTimeSaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopBusinessHourSaveCommand;
import com.tastyhouse.ceoapi.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.ceoapi.shop.response.ShopBusinessHourResponse;

/**
 * 점주용 영업시간·브레이크타임 관리 중개 서비스. 컨트롤러↔core 위임과 request→command / result→response 변환만 담당한다.
 *
 * <p><b>소유권 검증 한계</b>: 생성/목록 조회는 {@code shopId} 경로 변수로 소유권을 검증한다. 그러나 개별 수정/삭제는
 * {@code businessHourId}/{@code breakTimeId}만 경로에 있으므로, 대상을 먼저 조회해 그 소유 {@code shopId}로
 * 소유권을 검증한다(2단계 조회: 대상 조회 → 소유권 검증 → 실제 변경).
 */
@Service
@RequiredArgsConstructor
public class ShopBusinessHourService {

    private final ShopQueryService shopQueryService;
    private final ShopCommandService shopCommandService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public List<ShopBusinessHourResponse> getBusinessHours(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryService.findShopBusinessHours(shopId).stream()
            .map(this::toShopBusinessHourResponse)
            .toList();
    }

    public Long createBusinessHour(Long ceoId, Long shopId, String dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopBusinessHourSaveCommand command = ShopBusinessHourSaveCommand.of(DayType.from(dayType), openTime, closeTime, isClosed, is24Hours);
        ShopBusinessHour businessHour = shopCommandService.createBusinessHour(shopId, command);
        return businessHour.getId();
    }

    public void updateBusinessHour(Long ceoId, Long businessHourId, String dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        ShopBusinessHour businessHour = shopQueryService.findShopBusinessHourById(businessHourId);
        shopOwnershipValidator.validateOwnership(ceoId, businessHour.getShopId());
        ShopBusinessHourSaveCommand command = ShopBusinessHourSaveCommand.of(DayType.from(dayType), openTime, closeTime, isClosed, is24Hours);
        shopCommandService.updateBusinessHour(businessHourId, command);
    }

    public void deleteBusinessHour(Long ceoId, Long businessHourId) {
        ShopBusinessHour businessHour = shopQueryService.findShopBusinessHourById(businessHourId);
        shopOwnershipValidator.validateOwnership(ceoId, businessHour.getShopId());
        shopCommandService.deleteBusinessHour(businessHourId);
    }

    public List<ShopBreakTimeResponse> getBreakTimes(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopQueryService.findShopBreakTimes(shopId).stream()
            .map(this::toShopBreakTimeResponse)
            .toList();
    }

    public Long createBreakTime(Long ceoId, Long shopId, String dayType, LocalTime startTime, LocalTime endTime) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopBreakTimeSaveCommand command = ShopBreakTimeSaveCommand.of(DayType.from(dayType), startTime, endTime);
        ShopBreakTime breakTime = shopCommandService.createBreakTime(shopId, command);
        return breakTime.getId();
    }

    public void updateBreakTime(Long ceoId, Long breakTimeId, String dayType, LocalTime startTime, LocalTime endTime) {
        ShopBreakTime breakTime = shopQueryService.findShopBreakTimeById(breakTimeId);
        shopOwnershipValidator.validateOwnership(ceoId, breakTime.getShopId());
        ShopBreakTimeSaveCommand command = ShopBreakTimeSaveCommand.of(DayType.from(dayType), startTime, endTime);
        shopCommandService.updateBreakTime(breakTimeId, command);
    }

    public void deleteBreakTime(Long ceoId, Long breakTimeId) {
        ShopBreakTime breakTime = shopQueryService.findShopBreakTimeById(breakTimeId);
        shopOwnershipValidator.validateOwnership(ceoId, breakTime.getShopId());
        shopCommandService.deleteBreakTime(breakTimeId);
    }

    private ShopBusinessHourResponse toShopBusinessHourResponse(ShopBusinessHour businessHour) {
        return ShopBusinessHourResponse.from(
            businessHour.getId(),
            businessHour.getDayType().name(),
            businessHour.getDayType().getDescription(),
            businessHour.getOpenTime(),
            businessHour.getCloseTime(),
            businessHour.getIsClosed(),
            businessHour.getIs24Hours()
        );
    }

    private ShopBreakTimeResponse toShopBreakTimeResponse(ShopBreakTime breakTime) {
        return ShopBreakTimeResponse.from(
            breakTime.getId(),
            breakTime.getDayType().name(),
            breakTime.getDayType().getDescription(),
            breakTime.getStartTime(),
            breakTime.getEndTime()
        );
    }
}
