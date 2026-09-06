package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;

import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistoryResult;
import com.tastyhouse.domain.shared.page.PageResult;

@CeoApp
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
