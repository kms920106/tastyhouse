package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;

/**
 * {@link ShopDeliveryTipCalculator}의 입력 일체.
 *
 * <p><b>거리·행정동·공휴일 여부를 이미 해석된 값으로 받는 것이 계산기 순수성의 핵심이다</b> —
 * 좌표→거리 변환({@code GeoDistance})과 날짜→공휴일 판정({@code PublicHolidayCalendar})은 호출부가
 * 수행하므로, 계산기는 리포지토리도 시계도 갖지 않는 순수 함수로 남는다.
 *
 * <p>입력이 11개라 파라미터 나열 대신 record로 묶었다 — 위치 착오로 값이 조용히 뒤바뀌는 것을 막고,
 * 입력이 늘어도 시그니처 하나만 바뀐다.
 *
 * @param orderMethod                  주문 방법. {@code DELIVERY}가 아니면 배달팁은 전액 0
 * @param orderAmountAfterProductDiscount 구간 판정 기준 = 상품 할인 후 금액(쿠폰·포인트 차감 전)
 * @param deliveryDistanceMeters       가게~배달지 직선거리(m). {@code null}이면 거리별 미적용
 * @param deliveryAdminDongId          배달지 행정동. {@code null}이면 지역별 미적용
 * @param orderedAt                    시간별 판정 기준 시각(주문 접수 시점의 서버 시각)
 * @param publicHoliday                공휴일 캘린더가 판정한 값
 * @param setting                      배달팁 설정 헤더. nullable(미설정 가게)
 * @param tiers                        구간별 배달팁 전체
 * @param regionTips                   지역별 배달팁 전체
 * @param scheduleTips                 시간별 배달팁 전체
 * @param holidayTip                   공휴일 배달팁. nullable(미설정)
 */
public record ShopDeliveryTipContext(
    OrderMethod orderMethod,
    int orderAmountAfterProductDiscount,
    Double deliveryDistanceMeters,
    AdminDongId deliveryAdminDongId,
    LocalDateTime orderedAt,
    boolean publicHoliday,
    ShopDeliveryTipSetting setting,
    List<ShopDeliveryTipTier> tiers,
    List<ShopDeliveryTipRegion> regionTips,
    List<ShopDeliveryTipSchedule> scheduleTips,
    ShopDeliveryTipHoliday holidayTip
) {

    /** 컬렉션 인자의 {@code null}은 빈 목록으로 정규화한다 — 계산기에 null 분기를 남기지 않기 위함. */
    public ShopDeliveryTipContext {
        tiers = tiers == null ? List.of() : List.copyOf(tiers);
        regionTips = regionTips == null ? List.of() : List.copyOf(regionTips);
        scheduleTips = scheduleTips == null ? List.of() : List.copyOf(scheduleTips);
    }

    public static ShopDeliveryTipContext of(
        OrderMethod orderMethod,
        int orderAmountAfterProductDiscount,
        Double deliveryDistanceMeters,
        AdminDongId deliveryAdminDongId,
        LocalDateTime orderedAt,
        boolean publicHoliday,
        ShopDeliveryTipSetting setting,
        List<ShopDeliveryTipTier> tiers,
        List<ShopDeliveryTipRegion> regionTips,
        List<ShopDeliveryTipSchedule> scheduleTips,
        ShopDeliveryTipHoliday holidayTip
    ) {
        return new ShopDeliveryTipContext(
            orderMethod,
            orderAmountAfterProductDiscount,
            deliveryDistanceMeters,
            deliveryAdminDongId,
            orderedAt,
            publicHoliday,
            setting,
            tiers,
            regionTips,
            scheduleTips,
            holidayTip
        );
    }
}
