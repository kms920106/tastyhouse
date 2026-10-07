package com.tastyhouse.application.shop.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;

public interface ShopRequestListQueryUseCase {

    PageResult<ShopRequestListItemViewResult> getRequests(
        Long ceoId,
        Long shopId,
        String requestType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );
}
