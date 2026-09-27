package com.tastyhouse.application.ceo.port.out;

import java.time.LocalDate;

public record CeoLoginHistorySearchCondition(
    Long ceoId,
    String result,
    LocalDate startDate,
    LocalDate endDate
) {

    public static CeoLoginHistorySearchCondition of(
        Long ceoId,
        String result,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new CeoLoginHistorySearchCondition(
            ceoId,
            result,
            startDate,
            endDate
        );
    }
}
