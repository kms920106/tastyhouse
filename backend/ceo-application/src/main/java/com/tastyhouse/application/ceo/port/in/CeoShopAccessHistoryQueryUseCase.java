package com.tastyhouse.application.ceo.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistoryResult;

public interface CeoShopAccessHistoryQueryUseCase {

    PageResult<ShopCeoAssignmentHistoryResult> getShopAccessHistories(
        Long ceoId,
        String actionType,
        Long shopId,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );
}
