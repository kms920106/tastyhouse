package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;

import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryResult;
import com.tastyhouse.domain.shared.page.PageResult;

@CeoApp
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
