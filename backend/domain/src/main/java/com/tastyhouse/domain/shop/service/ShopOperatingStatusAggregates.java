package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

/**
 * 한 가게의 영업상태 판정 입력 중 <b>주문유형과 무관한 부분</b>(다섯 자식 애그리거트)을 담는 조회 중간값.
 *
 * <p>{@link ShopOperatingStatusService}가 유형별 상태를 판정할 때 같은 값을 배정된 유형 수만큼 재사용한다 —
 * 이 묶음을 한 번 조회해 두고 {@link #toContext}로 주문유형·시각만 갈아끼우면, 여섯 애그리거트 조회를
 * 유형마다 반복하지 않아도 된다(계산기는 순수 함수라 재호출 비용이 DB 왕복이 아니다).
 *
 * <p>같은 타입의 {@code List}가 다섯이라 위치를 착각해도 컴파일이 통과하므로, 생성부의 인자 순서를
 * 컴포넌트 선언 순서와 하나씩 대조한다.
 *
 * @param businessHours     가게 영업시간 전체
 * @param breakTimes        가게 휴게시간 전체
 * @param closedDays        정기휴무 전체
 * @param temporaryClosures 임시휴무 전체
 * @param suspensions       영업 임시중지 전체(유형별·전체 대상 모두 포함)
 */
public record ShopOperatingStatusAggregates(
    List<ShopBusinessHour> businessHours,
    List<ShopBreakTime> breakTimes,
    List<ShopClosedDay> closedDays,
    List<ShopTemporaryClosure> temporaryClosures,
    List<ShopSuspension> suspensions
) {

    public static ShopOperatingStatusAggregates of(
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions
    ) {
        return new ShopOperatingStatusAggregates(
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions
        );
    }

    /**
     * 이 묶음에 가게·주문유형·공휴일 여부·기준 시각을 더해 계산기 입력을 만든다.
     *
     * @param orderMethod null이면 가게 전체 판정
     */
    public ShopOperatingStatusContext toContext(
        Shop shop,
        OrderMethod orderMethod,
        boolean publicHoliday,
        LocalDateTime now
    ) {
        return ShopOperatingStatusContext.of(
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
