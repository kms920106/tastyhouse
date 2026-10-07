package com.tastyhouse.application.review.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewBlindRequestListQueryUseCase {

    PageResult<ReviewBlindRequestListItemResult> getBlindRequests(
        Long shopId,
        String status,
        String reason,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );
}
