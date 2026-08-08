package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

/**
 * {@link ShopOperatingStatusCalculator}의 입력 일체.
 *
 * <p>입력이 9개라 파라미터 나열 대신 record로 묶었다({@link ScheduledOrderSlotContext}·
 * {@code ShopDeliveryTipContext} 선례) — 같은 타입의 {@code List}가 다섯이라 위치를 착각해도 컴파일이
 * 통과하고 값만 조용히 뒤바뀌기 때문이다. 조회·조립은 {@link ShopOperatingStatusService}가 담당하고,
 * 계산기는 넘겨받은 값만으로 판정하는 순수 함수로 남는다.
 *
 * @param shop              폐업·노출정지·공휴일휴무 판정 대상 가게
 * @param businessHours     가게 영업시간 전체. 비어 있으면 영업중으로 본다(정보 미입력 ≠ 준비중)
 * @param breakTimes        가게 휴게시간 전체
 * @param closedDays        정기휴무 전체
 * @param temporaryClosures 임시휴무 전체
 * @param suspensions       영업 임시중지 전체
 * @param orderMethod       판정 대상 주문유형. <b>null이면 가게 전체 판정</b>이며, 이때 유형별 임시중지는
 *                          무시된다({@link ShopSuspension#appliesTo(OrderMethod)})
 * @param publicHoliday     기준 시각이 공휴일인지 여부
 * @param now               판정 기준 시각
 */
public record ShopOperatingStatusContext(
    Shop shop,
    List<ShopBusinessHour> businessHours,
    List<ShopBreakTime> breakTimes,
    List<ShopClosedDay> closedDays,
    List<ShopTemporaryClosure> temporaryClosures,
    List<ShopSuspension> suspensions,
    OrderMethod orderMethod,
    boolean publicHoliday,
    LocalDateTime now
) {

    /** 컬렉션 인자의 {@code null}은 빈 목록으로 정규화한다 — 계산기에 null 분기를 남기지 않기 위함. */
    public ShopOperatingStatusContext {
        businessHours = businessHours == null ? List.of() : List.copyOf(businessHours);
        breakTimes = breakTimes == null ? List.of() : List.copyOf(breakTimes);
        closedDays = closedDays == null ? List.of() : List.copyOf(closedDays);
        temporaryClosures = temporaryClosures == null ? List.of() : List.copyOf(temporaryClosures);
        suspensions = suspensions == null ? List.of() : List.copyOf(suspensions);
    }

    public static ShopOperatingStatusContext of(
        Shop shop,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions,
        OrderMethod orderMethod,
        boolean publicHoliday,
        LocalDateTime now
    ) {
        return new ShopOperatingStatusContext(
            shop,
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions,
            orderMethod,
            publicHoliday,
            now
        );
    }
}
