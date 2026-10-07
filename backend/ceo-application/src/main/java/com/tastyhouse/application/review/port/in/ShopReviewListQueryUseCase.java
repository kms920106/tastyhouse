package com.tastyhouse.application.review.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.review.port.out.ShopReviewListItemViewResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopReviewListQueryUseCase {

    PageResult<ShopReviewListItemViewResult> getReviews(
        Long ceoId,
        Long shopId,
        String tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        String sortType,
        int page,
        int size
    );
}
