package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

/**
 * {@link ScheduledOrderSlotCalculator}의 입력 일체.
 *
 * <p>입력이 8개라 파라미터 나열 대신 record로 묶었다({@code ShopDeliveryTipContext} 선례) — 같은 타입의
 * {@code List}가 넷이라 위치를 착각해도 컴파일이 통과하고 값만 조용히 뒤바뀌기 때문이다. 조회·조립은
 * {@link ScheduledOrderSlotService}가 담당하고, 계산기는 넘겨받은 값만으로 판정하는 순수 함수로 남는다.
 *
 * <p><b>공휴일 여부를 담지 않는 것이 배달팁 Context와 다른 점이다</b> — 슬롯 계산은 미래의 여러 시각을
 * 각각 판정해야 해서 "하나의 공휴일 여부"로 표현할 수 없다. 현재 정책은
 * {@code ShopOperatingStatusService}와 동일하게 {@code publicHoliday=false} 고정이므로 계산기 내부에서
 * 상수로 넘긴다(상세는 계산기 Javadoc).
 *
 * @param shop              예약주문 운영 여부·폐업·노출정지 판정 대상 가게
 * @param orderMethod       주문 방법. {@code DELIVERY}/{@code TAKEOUT}이 아니면 슬롯 없음
 * @param now               기준 시각(서버 시각). 리드타임 하한과 24시간 가게 상한의 기준
 * @param businessHours     가게 영업시간 전체. 비어 있으면 fail-safe로 슬롯 없음
 * @param breakTimes        가게 휴게시간 전체
 * @param closedDays        정기휴무 전체
 * @param temporaryClosures 임시휴무 전체
 * @param suspensions       영업 임시중지 전체
 * @param shopOrderMethods  가게에 배정된 주문유형 전체. {@code orderMethod}가 여기에 없으면 슬롯 없음 —
 *                          전역 정책 {@code ScheduledOrderPolicy.supports}는 "서비스가 예약주문을 지원하는
 *                          유형인가"만 보고 <b>그 가게가 그 유형을 취급하는가</b>는 보지 않기 때문이다
 */
public record ScheduledOrderSlotContext(
    Shop shop,
    OrderMethod orderMethod,
    LocalDateTime now,
    List<ShopBusinessHour> businessHours,
    List<ShopBreakTime> breakTimes,
    List<ShopClosedDay> closedDays,
    List<ShopTemporaryClosure> temporaryClosures,
    List<ShopSuspension> suspensions,
    List<ShopOrderMethod> shopOrderMethods
) {

    /** 컬렉션 인자의 {@code null}은 빈 목록으로 정규화한다 — 계산기에 null 분기를 남기지 않기 위함. */
    public ScheduledOrderSlotContext {
        businessHours = businessHours == null ? List.of() : List.copyOf(businessHours);
        breakTimes = breakTimes == null ? List.of() : List.copyOf(breakTimes);
        closedDays = closedDays == null ? List.of() : List.copyOf(closedDays);
        temporaryClosures = temporaryClosures == null ? List.of() : List.copyOf(temporaryClosures);
        suspensions = suspensions == null ? List.of() : List.copyOf(suspensions);
        shopOrderMethods = shopOrderMethods == null ? List.of() : List.copyOf(shopOrderMethods);
    }

    public static ScheduledOrderSlotContext of(
        Shop shop,
        OrderMethod orderMethod,
        LocalDateTime now,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions,
        List<ShopOrderMethod> shopOrderMethods
    ) {
        return new ScheduledOrderSlotContext(
            shop,
            orderMethod,
            now,
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions,
            shopOrderMethods
        );
    }

    /** 이 가게가 판정 대상 주문유형을 취급하는지 — 배정 목록에 그 유형의 행이 있으면 취급한다. */
    public boolean supportsOrderMethod() {
        return shopOrderMethods.stream()
            .anyMatch(assigned -> assigned.getOrderMethod() == orderMethod);
    }
}
