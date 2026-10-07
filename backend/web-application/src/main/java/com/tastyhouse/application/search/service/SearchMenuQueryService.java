package com.tastyhouse.application.search.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductKeywordSearchQueryUseCase;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.search.port.in.SearchMenuQueryUseCase;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class SearchMenuQueryService implements SearchMenuQueryUseCase {

    private final ProductKeywordSearchQueryUseCase productKeywordSearchQueryUseCase;

    public SearchMenuQueryService(ProductKeywordSearchQueryUseCase productKeywordSearchQueryUseCase) {
        this.productKeywordSearchQueryUseCase = productKeywordSearchQueryUseCase;
    }

    @Override
    public PageResult<SearchProductItemResult> searchMenus(String query, int page, int size) {
        String keyword = validateKeyword(query);
        return productKeywordSearchQueryUseCase.searchByKeyword(keyword, page, size);
    }

    private String validateKeyword(String query) {
        String keyword = query.strip();
        if (keyword.isBlank()) {
            throw new ApplicationException(WebErrorCode.SEARCH_KEYWORD_BLANK);
        }
        return keyword;
    }
}
