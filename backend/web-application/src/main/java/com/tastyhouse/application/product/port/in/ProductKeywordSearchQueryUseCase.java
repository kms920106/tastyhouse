package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductKeywordSearchQueryUseCase {

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, int page, int size);
}
