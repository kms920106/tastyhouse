package com.tastyhouse.application.search.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.search.port.in.SearchShopPublicQueryUseCase;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

@Service
@Transactional(readOnly = true)
class SearchShopPublicQueryService implements SearchShopPublicQueryUseCase {

    private final ShopSearchQueryPort shopSearchQueryPort;

    public SearchShopPublicQueryService(ShopSearchQueryPort shopSearchQueryPort) {
        this.shopSearchQueryPort = shopSearchQueryPort;
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> searchShopsPublic(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopSearchQueryPort.searchByKeywordWithBookmark(keyword, null, null, pageQuery);
    }

    private String validateKeyword(String query) {
        String keyword = query.strip();
        if (keyword.isBlank()) {
            throw new ApplicationException(WebErrorCode.SEARCH_KEYWORD_BLANK);
        }
        return keyword;
    }
}
