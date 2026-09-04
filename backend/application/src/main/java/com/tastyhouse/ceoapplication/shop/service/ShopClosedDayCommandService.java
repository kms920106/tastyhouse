package com.tastyhouse.ceoapplication.shop.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.service.ShopBusinessHourService;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.ceoapplication.shop.port.in.ShopClosedDayCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopClosedDayOwnerCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopClosedDayOwnerDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopHolidayClosureUpdateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopTemporaryClosureCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopTemporaryClosureDeleteCommand;

/**
 * 점주용 휴무(공휴일 토글·정기 휴무·임시 휴무) 변경 서비스(CQRS command 측).
 *
 * <p>정기휴무 최대 15건 제한은 도메인 서비스 {@link ShopBusinessHourService}, 공휴일 휴무 토글은
 * {@link ShopLifecycleService}가 담당한다. 임시휴무 누적 30일 제한은 이 서비스가 write 포트로
 * 기존 휴무를 읽어 검증한다(단일 애그리거트 연산이라 도메인 서비스로 하강하지 않음).
 *
 * <p><b>변경이력</b>: 정기휴무({@code CLOSED_DAY})·공휴일 휴무({@code HOLIDAY_CLOSURE})는 각각의 도메인
 * 서비스가 기록한다. 반면 <b>임시휴무({@code TEMPORARY_CLOSURE})는 대응 도메인 서비스가 없어</b> 이
 * 서비스가 write 포트로 직접 쓰므로, 이력도 여기서 {@link ShopChangeHistoryRecorder}로 직접 남긴다.
 *
 * <p><b>소유권 검증 한계</b>: 정기휴무·임시휴무 삭제는 경로에 shopId가 없고 소속 역조회 메서드도
 * 없어 ceo-api 계층에서는 소유권을 검증하지 않는다(기존 동작 유지). 다만 이력의 변경 주체를 남기기 위해
 * {@code ceoId}는 전달받는다.
 */
@Service
@Transactional
public class ShopClosedDayCommandService implements ShopClosedDayCommandUseCase {

    /**
     * 가게당 임시 휴무 누적 허용 일수.
     */
    private static final long MAX_ACCUMULATED_CLOSURE_DAYS = 30;

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopLifecycleService shopLifecycleService;
    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDayCommandService(
        ShopBusinessHourService shopBusinessHourService,
        ShopLifecycleService shopLifecycleService,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopLifecycleService = shopLifecycleService;
        this.shopTemporaryClosureRepository = shopTemporaryClosureRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateHolidayClosure(ShopHolidayClosureUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        boolean closedOnPublicHolidays = command.closedOnPublicHolidays();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopLifecycleService.updateHolidayClosure(targetShopId, closedOnPublicHolidays, actor);
    }

    @Override
    public Long createClosedDay(ShopClosedDayOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String closedDayType = command.closedDayType();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(
            shopId, ClosedDayType.from(closedDayType), actor
        );
        return closedDay.getId();
    }

    @Override
    public void deleteClosedDay(ShopClosedDayOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long closedDayId = command.closedDayId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.deleteClosedDay(closedDayId, actor);
    }

    /**
     * 임시 휴무를 등록한다. 가게의 기존 임시휴무 누적 일수와 합쳐
     * {@value #MAX_ACCUMULATED_CLOSURE_DAYS}일을 넘을 수 없다.
     */
    @Override
    public Long createTemporaryClosure(ShopTemporaryClosureCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        LocalDate startDate = command.startDate();
        LocalDate endDate = command.endDate();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopTemporaryClosure temporaryClosure = ShopTemporaryClosure.of(targetShopId, startDate, endDate);

        long accumulatedDays = shopTemporaryClosureRepository.findByShopId(shopId).stream()
            .mapToLong(ShopTemporaryClosure::days)
            .sum();
        if (accumulatedDays + temporaryClosure.days() > MAX_ACCUMULATED_CLOSURE_DAYS) {
            throw new BusinessException(ErrorCode.SHOP_TEMPORARY_CLOSURE_LIMIT_EXCEEDED);
        }

        ShopTemporaryClosure saved = shopTemporaryClosureRepository.save(temporaryClosure);

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.TEMPORARY_CLOSURE,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeTemporaryClosure(saved)
        );
        return saved.getId();
    }

    @Override
    public void deleteTemporaryClosure(ShopTemporaryClosureDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long temporaryClosureId = command.temporaryClosureId();

        ShopTemporaryClosure temporaryClosure = shopTemporaryClosureRepository.findById(temporaryClosureId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_TEMPORARY_CLOSURE_NOT_FOUND));
        shopTemporaryClosureRepository.deleteById(temporaryClosureId);

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopChangeHistoryRecorder.record(
            temporaryClosure.getShopId(),
            ShopChangeType.TEMPORARY_CLOSURE,
            ShopChangeActionType.DELETE,
            actor,
            describeTemporaryClosure(temporaryClosure),
            null
        );
    }

    /**
     * 임시휴무 1행을 한 줄로 요약한다(예: {@code "2026-08-11~2026-08-15"}).
     */
    private String describeTemporaryClosure(ShopTemporaryClosure temporaryClosure) {
        return ShopChangeValueFormatter.dateRange(temporaryClosure.getStartDate(), temporaryClosure.getEndDate());
    }
}
