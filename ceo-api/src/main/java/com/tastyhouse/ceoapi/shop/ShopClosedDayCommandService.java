package com.tastyhouse.ceoapi.shop;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ClosedDayType;
import com.tastyhouse.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.domain.shop.domain.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.domain.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.domain.service.ShopBusinessHourService;
import com.tastyhouse.domain.shop.domain.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 점주용 휴무(공휴일 토글·정기 휴무·임시 휴무) 변경 서비스(CQRS command 측).
 *
 * <p>정기휴무 최대 15건 제한은 도메인 서비스 {@link ShopBusinessHourService}, 공휴일 휴무 토글은
 * {@link ShopLifecycleService}가 담당한다. 임시휴무 누적 30일 제한은 이 서비스가 write 포트로
 * 기존 휴무를 읽어 검증한다(단일 애그리거트 연산이라 도메인 서비스로 하강하지 않음).
 *
 * <p><b>소유권 검증 한계</b>: 정기휴무·임시휴무 삭제는 경로에 shopId가 없고 소속 역조회 메서드도
 * 없어 ceo-api 계층에서는 소유권을 검증하지 않는다(기존 동작 유지).
 */
@Service
@Transactional
public class ShopClosedDayCommandService {

    /**
     * 가게당 임시 휴무 누적 허용 일수.
     */
    private static final long MAX_ACCUMULATED_CLOSURE_DAYS = 30;

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopLifecycleService shopLifecycleService;
    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDayCommandService(
        ShopBusinessHourService shopBusinessHourService,
        ShopLifecycleService shopLifecycleService,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopLifecycleService = shopLifecycleService;
        this.shopTemporaryClosureRepository = shopTemporaryClosureRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void updateHolidayClosure(Long ceoId, Long shopId, boolean closedOnPublicHolidays) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopId targetShopId = ShopId.of(shopId);
        shopLifecycleService.updateHolidayClosure(targetShopId, closedOnPublicHolidays);
    }

    public Long createClosedDay(Long ceoId, Long shopId, String closedDayType) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(shopId, ClosedDayType.from(closedDayType));
        return closedDay.getId();
    }

    public void deleteClosedDay(Long closedDayId) {
        shopBusinessHourService.deleteClosedDay(closedDayId);
    }

    /**
     * 임시 휴무를 등록한다. 가게의 기존 임시휴무 누적 일수와 합쳐
     * {@value #MAX_ACCUMULATED_CLOSURE_DAYS}일을 넘을 수 없다.
     */
    public Long createTemporaryClosure(Long ceoId, Long shopId, LocalDate startDate, LocalDate endDate) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopTemporaryClosure temporaryClosure = ShopTemporaryClosure.of(ShopId.of(shopId), startDate, endDate);

        long accumulatedDays = shopTemporaryClosureRepository.findByShopId(shopId).stream()
            .mapToLong(ShopTemporaryClosure::days)
            .sum();
        if (accumulatedDays + temporaryClosure.days() > MAX_ACCUMULATED_CLOSURE_DAYS) {
            throw new BusinessException(ErrorCode.SHOP_TEMPORARY_CLOSURE_LIMIT_EXCEEDED);
        }

        return shopTemporaryClosureRepository.save(temporaryClosure).getId();
    }

    public void deleteTemporaryClosure(Long temporaryClosureId) {
        shopTemporaryClosureRepository.findById(temporaryClosureId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_TEMPORARY_CLOSURE_NOT_FOUND));
        shopTemporaryClosureRepository.deleteById(temporaryClosureId);
    }
}
