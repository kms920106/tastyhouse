package com.tastyhouse.application.shop.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;

public interface ShopChangeHistoryListQueryUseCase {

    PageResult<ShopChangeHistoryResult> getChangeHistories(
        Long ceoId,
        Long shopId,
        String category,
        String changeType,
        LocalDate changedDate,
        int page,
        int size
    );
}
