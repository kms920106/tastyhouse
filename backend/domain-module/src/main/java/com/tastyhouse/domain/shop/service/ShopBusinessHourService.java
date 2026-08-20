package com.tastyhouse.domain.shop.service;

import java.time.LocalTime;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 영업시간·휴게시간·정기휴무 규격 불변식(도메인 서비스).
 *
 * <p><b>휴게시간은 같은 요일 영업시간 범위 안</b>이어야 하며(다른 애그리거트인 영업시간을 읽어 검증),
 * 정기휴무는 가게당 최대 {@value #MAX_REGULAR_CLOSED_DAY_COUNT}건까지만 등록할 수 있다. 이 규칙들은 등록
 * 액터(점주·관리자)가 달라도 동일해야 하므로 도메인 계층에 둔다(분류 C — 두 규칙 모두 자기 외의
 * 애그리거트·컬렉션을 읽어야 판정할 수 있는 크로스 애그리거트 규칙이다).
 *
 * <p>영업시간 규격(5분 단위, 최소 1시간~최대 23시간 55분)은 이 서비스가 아니라
 * {@link ShopBusinessHour#of}·{@code #update}가 강제한다 — 다른 애그리거트를 읽지 않는 <b>값 자체의
 * 불변식</b>이라, 서비스에 두면 {@code of()}를 직접 호출하는 경로(배치·마이그레이션)가 규격을 우회할 수
 * 있었기 때문이다.
 *
 * <p><b>변경이력 기록도 이 서비스가 소유한다</b>(운영 분류 {@code BUSINESS_HOUR}·{@code BREAK_TIME}
 * ·{@code CLOSED_DAY}). 불변식 검증을 위해 이미 애그리거트를 로드해 둔 상태라 <b>추가 조회 없이</b> 변경
 * 전 값을 얻을 수 있는 반면, 소비 모듈의 {@code CommandService}는 CQRS 교차 주입 금지로 QueryDao를
 * 주입할 수 없어 변경 전 값을 구조적으로 볼 수 없다. 변경 주체({@link ShopChangeActor})는 도메인이 인증을
 * 모르므로 마지막 파라미터로 명시 전달받는다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 */
public class ShopBusinessHourService {

    private static final int MAX_REGULAR_CLOSED_DAY_COUNT = 15;

    private final ShopDetailRepository shopDetailRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopBusinessHourService(
        ShopDetailRepository shopDetailRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDetailRepository = shopDetailRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public ShopBusinessHour createBusinessHour(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours,
        ShopChangeActor actor
    ) {
        ShopBusinessHour businessHour = ShopBusinessHour.of(
            ShopId.of(shopId), dayType, openTime, closeTime, isClosed, is24Hours
        );
        ShopBusinessHour saved = shopDetailRepository.saveBusinessHour(businessHour);

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.BUSINESS_HOUR,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeBusinessHour(saved)
        );
        return saved;
    }

    public void updateBusinessHour(
        Long id,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours,
        ShopChangeActor actor
    ) {
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        String previousValue = describeBusinessHour(businessHour);

        businessHour.update(dayType, openTime, closeTime, isClosed, is24Hours);
        shopDetailRepository.saveBusinessHour(businessHour);

        shopChangeHistoryRecorder.record(
            businessHour.getShopId(),
            ShopChangeType.BUSINESS_HOUR,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeBusinessHour(businessHour)
        );
    }

    /**
     * 영업시간을 삭제한다. 삭제 대상을 <b>먼저 로드</b>하는 이유는 이력에 필요한 소유 가게와 변경 전
     * 요약을 삭제 후에는 복원할 수 없기 때문이다. 대상이 없으면 기존 동작대로 아무 일도 하지 않는다
     * (식별자만으로 삭제하던 시절에도 존재하지 않는 id는 조용히 무시됐으므로, 새 에러코드를 만들지 않는다).
     */
    public void deleteBusinessHour(Long id, ShopChangeActor actor) {
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(id).orElse(null);
        if (businessHour == null) {
            return;
        }
        String previousValue = describeBusinessHour(businessHour);

        shopDetailRepository.deleteBusinessHourById(id);

        shopChangeHistoryRecorder.record(
            businessHour.getShopId(),
            ShopChangeType.BUSINESS_HOUR,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    public ShopBreakTime createBreakTime(
        Long shopId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        ShopChangeActor actor
    ) {
        validateBreakTimeWithinBusinessHours(shopId, dayType, startTime, endTime);
        ShopBreakTime breakTime = ShopBreakTime.of(ShopId.of(shopId), dayType, startTime, endTime);
        ShopBreakTime saved = shopDetailRepository.saveBreakTime(breakTime);

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.BREAK_TIME,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeBreakTime(saved)
        );
        return saved;
    }

    public void updateBreakTime(
        Long id,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        ShopChangeActor actor
    ) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        validateBreakTimeWithinBusinessHours(breakTime.getShopId().value(), dayType, startTime, endTime);
        String previousValue = describeBreakTime(breakTime);

        breakTime.update(dayType, startTime, endTime);
        shopDetailRepository.saveBreakTime(breakTime);

        shopChangeHistoryRecorder.record(
            breakTime.getShopId(),
            ShopChangeType.BREAK_TIME,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeBreakTime(breakTime)
        );
    }

    /**
     * 휴게시간을 삭제한다. 대상을 먼저 로드하는 이유와 미존재 시 무시하는 이유는
     * {@link #deleteBusinessHour(Long, ShopChangeActor)}와 같다.
     */
    public void deleteBreakTime(Long id, ShopChangeActor actor) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(id).orElse(null);
        if (breakTime == null) {
            return;
        }
        String previousValue = describeBreakTime(breakTime);

        shopDetailRepository.deleteBreakTimeById(id);

        shopChangeHistoryRecorder.record(
            breakTime.getShopId(),
            ShopChangeType.BREAK_TIME,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    /**
     * 정기휴무를 등록한다. 가게당 최대 {@value #MAX_REGULAR_CLOSED_DAY_COUNT}건을 넘을 수 없다.
     */
    public ShopClosedDay createClosedDay(Long shopId, ClosedDayType closedDayType, ShopChangeActor actor) {
        if (shopDetailRepository.findClosedDaysByShopId(shopId).size() >= MAX_REGULAR_CLOSED_DAY_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_REGULAR_CLOSED_DAY_LIMIT_EXCEEDED);
        }
        ShopClosedDay closedDay = ShopClosedDay.of(ShopId.of(shopId), closedDayType);
        ShopClosedDay saved = shopDetailRepository.saveClosedDay(closedDay);

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.CLOSED_DAY,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeClosedDay(saved)
        );
        return saved;
    }

    /**
     * 정기휴무를 삭제한다. 대상을 먼저 로드하는 이유와 미존재 시 무시하는 이유는
     * {@link #deleteBusinessHour(Long, ShopChangeActor)}와 같다.
     */
    public void deleteClosedDay(Long id, ShopChangeActor actor) {
        ShopClosedDay closedDay = shopDetailRepository.findClosedDayById(id).orElse(null);
        if (closedDay == null) {
            return;
        }
        String previousValue = describeClosedDay(closedDay);

        shopDetailRepository.deleteClosedDayById(id);

        shopChangeHistoryRecorder.record(
            closedDay.getShopId(),
            ShopChangeType.CLOSED_DAY,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    /**
     * 영업시간 1행을 사람이 읽을 수 있는 한 줄로 요약한다(예: {@code "매일 09:00~22:00"},
     * {@code "월요일 휴무"}, {@code "매일 24시간"}).
     *
     * <p>휴무·24시간은 시각이 의미를 갖지 않으므로 시각을 붙이지 않는다.
     */
    private String describeBusinessHour(ShopBusinessHour businessHour) {
        String dayLabel = businessHour.getDayType().getDescription();
        if (businessHour.isClosed()) {
            return dayLabel + " 휴무";
        }
        if (businessHour.is24Hours()) {
            return dayLabel + " 24시간";
        }
        return dayLabel + " " + ShopChangeValueFormatter.timeRange(businessHour.getOpenTime(), businessHour.getCloseTime());
    }

    /**
     * 휴게시간 1행을 한 줄로 요약한다(예: {@code "평일 15:00~17:00"}).
     */
    private String describeBreakTime(ShopBreakTime breakTime) {
        return breakTime.getDayType().getDescription() + " "
            + ShopChangeValueFormatter.timeRange(breakTime.getStartTime(), breakTime.getEndTime());
    }

    /**
     * 정기휴무 1행을 한 줄로 요약한다(예: {@code "매주 월요일"}).
     */
    private String describeClosedDay(ShopClosedDay closedDay) {
        return closedDay.getClosedDayType().getDescription();
    }

    /**
     * 휴게시간이 같은 요일 영업시간 범위 안에 있는지 검증한다(자정 넘김 반영). 영업시간과 완전히 동일하면 거부한다.
     */
    private void validateBreakTimeWithinBusinessHours(Long shopId, DayType dayType, LocalTime breakStart, LocalTime breakEnd) {
        if (breakStart == null || breakEnd == null) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHoursByShopId(shopId).stream()
            .filter(bh -> bh.getDayType() == dayType)
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS));
        if (businessHour.isClosed()) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        if (businessHour.is24Hours()) {
            return; // 24시간 영업이면 어떤 휴게시간도 범위 내
        }
        LocalTime open = businessHour.getOpenTime();
        LocalTime close = businessHour.getCloseTime();
        if (open != null && close != null && open.equals(breakStart) && close.equals(breakEnd)) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_EQUALS_BUSINESS_HOURS);
        }
        if (isOutside(open, close, breakStart) || isOutside(open, close, breakEnd)) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
    }

    /**
     * target이 [open, close] 영업 구간 밖에 있는지 판정한다(자정 넘김 구간이면 두 조각으로 나눠 판정).
     */
    private boolean isOutside(LocalTime open, LocalTime close, LocalTime target) {
        if (open == null || close == null) {
            return true;
        }
        if (open.isBefore(close)) {
            return target.isBefore(open) || target.isAfter(close);
        }
        // 자정 넘김: open~24:00 또는 00:00~close
        return target.isBefore(open) && target.isAfter(close);
    }
}
