package com.tastyhouse.domain.reservation.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class SlotPolicy {
    public static final int CAPACITY_PER_SLOT = 10;
    public static final LocalTime OPEN = LocalTime.of(10, 30);
    public static final LocalTime CLOSE = LocalTime.of(19, 30);
    public static final int INTERVAL_MINUTES = 30;

    private SlotPolicy() {
    }

    public static List<LocalTime> allSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime time = OPEN;
        while (!time.isAfter(CLOSE)) {
            slots.add(time);
            time = time.plusMinutes(INTERVAL_MINUTES);
        }
        return slots;
    }

    public static boolean isValidSlot(LocalTime time) {
        return time != null && allSlots().contains(time);
    }
}
