package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopChangeCategoryResult;

@CeoApp
public interface ShopChangeHistoryQueryUseCase {

    PageResult<ShopChangeHistoryResult> getChangeHistories(
        Long ceoId,
        Long shopId,
        String category,
        String changeType,
        LocalDate changedDate,
        int page,
        int size
    );

    List<ShopChangeCategoryResult> getChangeHistoryTypes();
}
