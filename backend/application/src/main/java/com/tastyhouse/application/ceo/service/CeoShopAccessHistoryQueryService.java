package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoShopAccessHistoryQueryUseCase;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistoryQueryPort;
import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistorySearchCondition;

@Service
@CeoApp
@Transactional(readOnly = true)
public class CeoShopAccessHistoryQueryService implements CeoShopAccessHistoryQueryUseCase {

    private static final int RETENTION_YEARS = 5;

    private static final int DEFAULT_RANGE_YEARS = 1;

    private final ShopCeoAssignmentHistoryQueryPort shopCeoAssignmentHistoryQueryPort;

    public CeoShopAccessHistoryQueryService(
        ShopCeoAssignmentHistoryQueryPort shopCeoAssignmentHistoryQueryPort
    ) {
        this.shopCeoAssignmentHistoryQueryPort = shopCeoAssignmentHistoryQueryPort;
    }

    @Override
    public PageResult<ShopCeoAssignmentHistoryResult> getShopAccessHistories(
        Long ceoId,
        String actionType,
        Long shopId,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedEndDate = endDate == null ? today : endDate;
        LocalDate resolvedStartDate = startDate == null
            ? resolvedEndDate.minusYears(DEFAULT_RANGE_YEARS)
            : startDate;
        validateDateRange(resolvedStartDate, resolvedEndDate, today);

        ShopCeoAssignmentActionType actionTypeFilter = actionType == null
            ? null
            : ShopCeoAssignmentActionType.from(actionType);

        ShopCeoAssignmentHistorySearchCondition condition = ShopCeoAssignmentHistorySearchCondition.of(
            ceoId,
            shopId,
            actionTypeFilter,
            resolvedStartDate,
            resolvedEndDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return shopCeoAssignmentHistoryQueryPort.findShopAccessHistoryPage(condition, pageQuery);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
        if (endDate.isAfter(today) || startDate.isBefore(today.minusYears(RETENTION_YEARS))) {
            throw new BusinessException(ErrorCode.CEO_SHOP_ACCESS_HISTORY_DATE_OUT_OF_RANGE);
        }
    }
}
