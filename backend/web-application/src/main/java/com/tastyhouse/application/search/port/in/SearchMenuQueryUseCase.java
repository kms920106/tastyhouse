package com.tastyhouse.application.search.port.in;

import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface SearchMenuQueryUseCase {

    PageResult<SearchProductItemResult> searchMenus(String query, int page, int size);
}
