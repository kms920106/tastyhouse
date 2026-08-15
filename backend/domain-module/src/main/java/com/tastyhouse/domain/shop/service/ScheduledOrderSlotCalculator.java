package com.tastyhouse.domain.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;

/**
 * 예약 가능한 수령 시간 슬롯 목록을 계산하는 순수 계산기.
 *
 * <p>리포지토리에 의존하지 않아 Spring/DB 없이 단위 테스트할 수 있다. 조회·조립은
 * {@link ScheduledOrderSlotService}가 담당하고, 이 계산기는 {@link ScheduledOrderSlotContext}로 넘겨받은
 * 값만으로 판정한다.
 *
 * <p><b>영업 판정을 새로 짜지 않는 것이 이 클래스의 핵심 설계다.</b>
 * {@link ShopOperatingStatusCalculator#calculate}가 이미 시각을 파라미터로 받으므로 미래 시각을 그대로
 * 넘긴다 — 폐업/노출정지/임시중지/공휴일휴무/임시휴무/정기휴무/영업시간/휴게시간 <b>8단계 우선순위가
 * 통째로 재사용</b>된다. 그래서 이 계산기는 생성자로 그 계산기를 받는다.
 *
 * <p>알고리즘:
 * <ol>
 *   <li>예약주문 미운영 / 미지원 주문방식 / 영업시간 미등록이면 빈 목록</li>
 *   <li>하한 = {@code ceil30(max(now, 오늘 영업 시작 시각) + 리드타임)}</li>
 *   <li>상한 = 24시간 영업이면 {@code now + 24h}, 아니면 오늘 영업 종료 시각(자정 넘김이면 익일 새벽)</li>
 *   <li>하한~상한을 30분 그리드로 훑으며 각 후보를 영업 판정에 태운다. 배달은 슬롯 <b>끝 직전</b>도 함께
 *       통과해야 한다 — 휴게시간·영업종료가 슬롯 중간에 걸치는 슬롯을 배제하기 위함이다</li>
 * </ol>
 *
 * <p><b>공휴일은 {@code false} 고정이다</b> — {@code ShopOperatingStatusService}가 이미 같은 정책이며
 * (코드베이스에 공휴일 캘린더를 영업상태에 연결한 지점이 없다), 예약만 먼저 연동하면 명절 예약이 통째로
 * 막힌다. 일원화는 별도 과제이고, 그때 이 상수와 그 서비스의 상수를 함께 교체한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다.
 */
public class ScheduledOrderSlotCalculator {

    /** 영업상태 판정과 동일한 공휴일 정책({@code ShopOperatingStatusService#PUBLIC_HOLIDAY}와 함께 교체한다). */
    private static final boolean PUBLIC_HOLIDAY = false;

    /**
     * 30분 그리드 훑기의 최대 반복 횟수 — 24시간 가게(48슬롯)에 자정 넘김 여유를 더한 상한이다.
     * 상·하한 계산에 결함이 생겨도 무한 루프가 되지 않게 막는 방어선이며, 정상 경로에서는 도달하지 않는다.
     */
    private static final int MAX_SLOT_CANDIDATES = 200;

    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ScheduledOrderSlotCalculator(ShopOperatingStatusCalculator shopOperatingStatusCalculator) {
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    /**
     * 예약 가능한 슬롯 목록을 시작 시각 오름차순으로 돌려준다. 예약할 수 없는 상태면 빈 목록이다
     * (예외를 던지지 않는다 — "예약 불가"는 오류가 아니라 정상적인 조회 결과다).
     */
    public List<ScheduledOrderSlot> calculate(ScheduledOrderSlotContext context) {
        Shop shop = context.shop();
        OrderMethod orderMethod = context.orderMethod();

        if (!shop.isScheduledOrderEnabled() || !ScheduledOrderPolicy.supports(orderMethod)) {
            return List.of();
        }
        // 전역 정책이 지원하는 유형이어도 그 가게가 취급하지 않으면 슬롯이 없다 —
        // 배달을 하지 않는 가게에 배달 예약 슬롯을 돌려주지 않기 위함.
        if (!context.supportsOrderMethod()) {
            return List.of();
        }
        // 영업시간이 없으면 "영업 시작 + 리드타임" 하한을 구할 수 없다. 영업상태 판정은 정보 미입력을
        // OPEN으로 보지만(준비중 오판 방지), 예약은 근거 없이 열 수 없으므로 fail-safe로 닫는다.
        if (context.businessHours().isEmpty()) {
            return List.of();
        }

        LocalDateTime now = context.now();
        ShopBusinessHour todayHour = shopOperatingStatusCalculator.selectApplicableHour(
            context.businessHours(), now.getDayOfWeek(), PUBLIC_HOLIDAY
        );
        if (todayHour == null || todayHour.isClosed()) {
            return List.of();
        }

        LocalDateTime earliest = earliestSlotStart(now, todayHour, orderMethod);
        LocalDateTime latest = latestSlotStart(now, todayHour);
        if (earliest.isAfter(latest)) {
            return List.of();
        }

        return collectOpenSlots(context, earliest, latest);
    }

    /**
     * 예약 가능 하한 — {@code max(now, 오늘 영업 시작 시각) + 리드타임}을 30분 단위로 올림한다.
     *
     * <p>기준이 {@code now}가 아니라 <b>영업 시작 시각과의 최댓값</b>인 것이 PDF 규격의 핵심이다:
     * "영업 시작 시각 2시간 이후부터"이므로, 오픈 전에 조회해도 오픈 직후 구간은 예약할 수 없다.
     * 24시간 영업 행은 시작 시각이 없으므로 {@code now}만 기준으로 한다.
     */
    private LocalDateTime earliestSlotStart(LocalDateTime now, ShopBusinessHour todayHour, OrderMethod orderMethod) {
        LocalDateTime base = now;
        if (!todayHour.is24Hours() && todayHour.getOpenTime() != null) {
            LocalDateTime openAt = LocalDateTime.of(now.toLocalDate(), todayHour.getOpenTime());
            if (openAt.isAfter(base)) {
                base = openAt;
            }
        }
        return ceilToSlotUnit(base.plusMinutes(ScheduledOrderPolicy.leadTimeMinutes(orderMethod)));
    }

    /**
     * 예약 가능 상한(마지막 슬롯이 시작할 수 있는 시각).
     *
     * <p>24시간 영업은 당일 제한 대신 {@code now + 24h}까지 연다(PDF 예외 규격). 그 외에는 오늘 영업 종료
     * 시각까지이며, 자정을 넘기는 영업(종료 &lt; 시작)은 익일 새벽 종료 시각으로 해석한다.
     *
     * <p>배달은 슬롯이 30분 범위라 <b>종료 시각에 걸치지 않도록</b> 30분을 뺀 시각이 마지막 시작점이다.
     * 포장은 단일 시각이지만 영업시간이 {@code [open, close)} 반열림이라 종료 시각 자체는 영업 중이 아니다 —
     * 두 경우 모두 종료 시각 30분 전이 마지막 후보가 되므로 여기서는 공통으로 30분을 뺀다.
     */
    private LocalDateTime latestSlotStart(LocalDateTime now, ShopBusinessHour todayHour) {
        if (todayHour.is24Hours()) {
            return now.plusHours(ScheduledOrderPolicy.MAX_HORIZON_HOURS_FOR_24H_SHOP);
        }

        LocalTime closeTime = todayHour.getCloseTime();
        LocalTime openTime = todayHour.getOpenTime();
        if (closeTime == null || openTime == null) {
            return now.plusHours(ScheduledOrderPolicy.MAX_HORIZON_HOURS_FOR_24H_SHOP);
        }

        LocalDate closeDate = closeTime.isAfter(openTime) ? now.toLocalDate() : now.toLocalDate().plusDays(1);
        return LocalDateTime.of(closeDate, closeTime).minusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES);
    }

    /**
     * 하한부터 상한까지 30분 그리드를 훑으며 영업 중인 슬롯만 모은다.
     *
     * <p>배달은 슬롯 끝 <b>직전</b> 시각(=끝 -1분)도 함께 영업 중이어야 한다. 슬롯 끝 시각 자체를 보면
     * 영업이 정확히 그때 끝나는 경우(21:30~22:00 슬롯 / 22:00 마감)를 잘못 배제한다 — 반열림 구간
     * {@code [open, close)}에서 종료 시각은 이미 영업 밖이기 때문이다.
     */
    private List<ScheduledOrderSlot> collectOpenSlots(
        ScheduledOrderSlotContext context,
        LocalDateTime earliest,
        LocalDateTime latest
    ) {
        List<ScheduledOrderSlot> slots = new ArrayList<>();
        boolean rangeSlot = ScheduledOrderPolicy.isRangeSlot(context.orderMethod());

        LocalDateTime candidate = earliest;
        for (int i = 0; i < MAX_SLOT_CANDIDATES && !candidate.isAfter(latest); i++) {
            if (isOpenAt(context, candidate)
                && (!rangeSlot || isOpenAt(context, candidate.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES - 1)))) {
                slots.add(ScheduledOrderSlot.of(context.orderMethod(), candidate));
            }
            candidate = candidate.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES);
        }
        return List.copyOf(slots);
    }

    /**
     * 기존 영업상태 계산기에 미래 시각을 그대로 넘겨 판정한다 — 8단계 우선순위를 통째로 재사용하는 지점이다.
     *
     * <p><b>주문유형을 함께 넘기는 것이 핵심이다</b> — 넘기지 않으면 배달만 임시중지한 가게에서 포장
     * 예약 슬롯까지 사라진다. 재사용 지점의 입력을 정확히 맞춰야 유형별 중지가 그 유형에만 걸린다.
     */
    private boolean isOpenAt(ScheduledOrderSlotContext context, LocalDateTime at) {
        return shopOperatingStatusCalculator.calculate(
            ShopOperatingStatusContext.of(
                context.shop(),
                context.businessHours(),
                context.breakTimes(),
                context.closedDays(),
                context.temporaryClosures(),
                context.suspensions(),
                context.orderMethod(),
                PUBLIC_HOLIDAY,
                at
            )
        ).isOpen();
    }

    /**
     * 30분 단위로 올림한다. 이미 정확히 30분 단위(초·나노 0 포함)면 그대로 둔다.
     *
     * <p>초·나노를 버린 뒤 분 나머지만 보면 {@code 12:00:30}처럼 <b>단위는 맞지만 초가 남은</b> 시각이
     * 나머지 0으로 판정돼 그대로 통과한다 — 실제로는 12:00을 이미 지났으므로 12:30으로 올라가야 한다.
     * 그래서 나머지가 0이어도 버려진 초·나노가 있으면 한 칸 올린다.
     */
    private static LocalDateTime ceilToSlotUnit(LocalDateTime dateTime) {
        LocalDateTime truncated = dateTime.withSecond(0).withNano(0);
        int remainder = truncated.getMinute() % ScheduledOrderPolicy.SLOT_UNIT_MINUTES;
        if (remainder == 0) {
            return dateTime.equals(truncated)
                ? truncated
                : truncated.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES);
        }
        return truncated.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES - remainder);
    }
}
