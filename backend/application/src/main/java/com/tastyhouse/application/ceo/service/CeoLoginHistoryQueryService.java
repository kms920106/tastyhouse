package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryQueryUseCase;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryResult;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistorySearchCondition;

@Service
@CeoApp
@Transactional(readOnly = true)
public class CeoLoginHistoryQueryService implements CeoLoginHistoryQueryUseCase {

    private static final int RETENTION_DAYS = 90;

    private static final int DEFAULT_RANGE_DAYS = 29;

    private final CeoLoginHistoryQueryPort ceoLoginHistoryQueryPort;

    public CeoLoginHistoryQueryService(CeoLoginHistoryQueryPort ceoLoginHistoryQueryPort) {
        this.ceoLoginHistoryQueryPort = ceoLoginHistoryQueryPort;
    }

    @Override
    public PageResult<CeoLoginHistoryResult> getLoginHistories(
        Long ceoId,
        String result,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedEndDate = endDate == null ? today : endDate;
        LocalDate resolvedStartDate = startDate == null
            ? resolvedEndDate.minusDays(DEFAULT_RANGE_DAYS)
            : startDate;
        validateDateRange(resolvedStartDate, resolvedEndDate, today);

        CeoLoginResult resultFilter = result == null ? null : CeoLoginResult.from(result);

        CeoLoginHistorySearchCondition condition = CeoLoginHistorySearchCondition.of(
            ceoId,
            resultFilter,
            resolvedStartDate,
            resolvedEndDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return ceoLoginHistoryQueryPort.findLoginHistoryPage(condition, pageQuery);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
        if (endDate.isAfter(today) || startDate.isBefore(today.minusDays(RETENTION_DAYS))) {
            throw new BusinessException(ErrorCode.CEO_LOGIN_HISTORY_DATE_OUT_OF_RANGE);
        }
    }
}
