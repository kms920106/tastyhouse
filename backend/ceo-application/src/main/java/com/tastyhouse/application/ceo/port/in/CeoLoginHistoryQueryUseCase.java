package com.tastyhouse.application.ceo.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface CeoLoginHistoryQueryUseCase {

    PageResult<CeoLoginHistoryResult> getLoginHistories(
        Long ceoId,
        String result,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );
}
