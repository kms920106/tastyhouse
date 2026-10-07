package com.tastyhouse.application.search.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.search.port.in.SearchShopQueryUseCase;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

@Service
@Transactional(readOnly = true)
class SearchShopQueryService implements SearchShopQueryUseCase {

    private final ShopSearchQueryPort shopSearchQueryPort;
    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;

    public SearchShopQueryService(
        ShopSearchQueryPort shopSearchQueryPort,
        MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort
    ) {
        this.shopSearchQueryPort = shopSearchQueryPort;
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> searchShopsPaged(String query, Long memberId, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        Long deliveryAdminDongId = memberDeliveryAddressQueryPort
            .findDefaultAdminDongId(MemberId.of(memberId).value())
            .orElse(null);
        return shopSearchQueryPort.searchByKeywordWithBookmark(keyword, memberId, deliveryAdminDongId, pageQuery);
    }

    private String validateKeyword(String query) {
        String keyword = query.strip();
        if (keyword.isBlank()) {
            throw new ApplicationException(WebErrorCode.SEARCH_KEYWORD_BLANK);
        }
        return keyword;
    }
}
