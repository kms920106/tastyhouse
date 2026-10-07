package com.tastyhouse.application.search.port.in;

import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface SearchReviewQueryUseCase {

    PageResult<SearchReviewItemResult> searchReviews(String query, int page, int size);
}
