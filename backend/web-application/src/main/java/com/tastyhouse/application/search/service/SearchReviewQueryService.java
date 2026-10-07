package com.tastyhouse.application.search.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import com.tastyhouse.application.search.port.in.SearchReviewQueryUseCase;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class SearchReviewQueryService implements SearchReviewQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;

    public SearchReviewQueryService(ReviewQueryPort reviewQueryPort) {
        this.reviewQueryPort = reviewQueryPort;
    }

    @Override
    public PageResult<SearchReviewItemResult> searchReviews(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewQueryPort.searchByKeyword(keyword, pageQuery);
    }

    private String validateKeyword(String query) {
        String keyword = query.strip();
        if (keyword.isBlank()) {
            throw new ApplicationException(WebErrorCode.SEARCH_KEYWORD_BLANK);
        }
        return keyword;
    }
}
