package com.tastyhouse.domain.reservation.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 고정 예약 슬롯 정의.
 * 화면 기준 10:30 ~ 19:30, 30분 간격 슬롯과 슬롯당 정원을 코드 상수로 관리한다.
 * 정원 단위는 "예약 팀(건) 수"이다. ([[plan]] #5 확정)
 */
public final class SlotPolicy {

    public static final int CAPACITY_PER_SLOT = 10; // 슬롯당 최대 예약 팀(건) 수
    public static final LocalTime OPEN = LocalTime.of(10, 30);
    public static final LocalTime CLOSE = LocalTime.of(19, 30);
    public static final int INTERVAL_MINUTES = 30;

    private SlotPolicy() {
    }

    /**
     * OPEN ~ CLOSE 사이를 INTERVAL_MINUTES 간격으로 분할한 전체 슬롯 시간 목록.
     */
    public static List<LocalTime> allSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime time = OPEN;
        while (!time.isAfter(CLOSE)) {
            slots.add(time);
            time = time.plusMinutes(INTERVAL_MINUTES);
        }
        return slots;
    }

    /**
     * 주어진 시간이 유효한 슬롯 시간인지 검증.
     */
    public static boolean isValidSlot(LocalTime time) {
        return time != null && allSlots().contains(time);
    }
}
