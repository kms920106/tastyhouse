package com.tastyhouse.application.ceo.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.ceo.model.CeoLoginResult;

public record CeoLoginHistorySearchCondition(
    Long ceoId,
    CeoLoginResult result,
    LocalDate startDate,
    LocalDate endDate
) {

    public static CeoLoginHistorySearchCondition of(
        Long ceoId,
        CeoLoginResult result,
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
