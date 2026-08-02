package com.tastyhouse.ceoapi.shop;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.DayType;
import com.tastyhouse.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.domain.service.ShopBusinessHourService;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 점주용 영업시간·휴게시간 변경 서비스(CQRS command 측).
 *
 * <p>영업시간 규격(5분 단위·최소 1시간)·휴게시간 범위 등 불변식은 도메인 서비스
 * {@link ShopBusinessHourService}가 담당하고, 이 서비스는 트랜잭션 경계와 소유권 검증, 그리고
 * 경계 타입 승격(String → {@link DayType})만 담당한다.
 *
 * <p><b>소유권 검증 한계</b>: 생성은 {@code shopId} 경로 변수로 소유권을 검증한다. 개별 수정/삭제는
 * {@code businessHourId}/{@code breakTimeId}만 경로에 있으므로, 대상을 먼저 조회해 그 소유
 * {@code shopId}로 소유권을 검증한다(2단계 조회: 대상 조회 → 소유권 검증 → 실제 변경).
 */
@Service
@Transactional
public class ShopBusinessHourCommandService {

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

    public Long createBusinessHour(
        Long ceoId,
        Long shopId,
        String dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopBusinessHour businessHour = shopBusinessHourService.createBusinessHour(
            shopId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours
        );
        return businessHour.getId();
    }

    public void updateBusinessHour(
        Long ceoId,
        Long businessHourId,
        String dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        validateBusinessHourOwnership(ceoId, businessHourId);
        shopBusinessHourService.updateBusinessHour(
            businessHourId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours
        );
    }

    public void deleteBusinessHour(Long ceoId, Long businessHourId) {
        validateBusinessHourOwnership(ceoId, businessHourId);
        shopBusinessHourService.deleteBusinessHour(businessHourId);
    }

    public Long createBreakTime(Long ceoId, Long shopId, String dayType, LocalTime startTime, LocalTime endTime) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopBreakTime breakTime = shopBusinessHourService.createBreakTime(
            shopId, DayType.from(dayType), startTime, endTime
        );
        return breakTime.getId();
    }

    public void updateBreakTime(Long ceoId, Long breakTimeId, String dayType, LocalTime startTime, LocalTime endTime) {
        validateBreakTimeOwnership(ceoId, breakTimeId);
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime);
    }

    public void deleteBreakTime(Long ceoId, Long breakTimeId) {
        validateBreakTimeOwnership(ceoId, breakTimeId);
        shopBusinessHourService.deleteBreakTime(breakTimeId);
    }

    /**
     * 영업시간 단건의 소유 가게로 점주 소유권을 검증한다.
     */
    private void validateBusinessHourOwnership(Long ceoId, Long businessHourId) {
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(businessHourId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, businessHour.getShopId().value());
    }

    /**
     * 휴게시간 단건의 소유 가게로 점주 소유권을 검증한다.
     */
    private void validateBreakTimeOwnership(Long ceoId, Long breakTimeId) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(breakTimeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, breakTime.getShopId().value());
    }
}
