package com.tastyhouse.application.review.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.review.port.out.ReviewBlindRequestDetailResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@AdminApp
public interface ReviewBlindRequestQueryUseCase {

    PageResult<ReviewBlindRequestListItemResult> getBlindRequests(
        Long shopId,
        String status,
        String reason,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );

    ReviewBlindRequestDetailResult getBlindRequest(Long id);
}
