package com.tastyhouse.application.search.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import com.tastyhouse.application.search.port.out.PopularKeywordResult;
import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;
import com.tastyhouse.application.search.port.out.SearchQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;
import com.tastyhouse.application.product.service.ProductQueryService;
import com.tastyhouse.application.search.port.in.SearchQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class SearchQueryService implements SearchQueryUseCase {

    private final SearchQueryPort searchQueryPort;
    private final ProductQueryService productQueryService;
    private final ReviewQueryPort reviewQueryPort;
    private final ShopSearchQueryPort shopSearchQueryPort;
    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;

    public SearchQueryService(
        SearchQueryPort searchQueryPort,
        ProductQueryService productQueryService,
        ReviewQueryPort reviewQueryPort,
        ShopSearchQueryPort shopSearchQueryPort,
        MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort
    ) {
        this.searchQueryPort = searchQueryPort;
        this.productQueryService = productQueryService;
        this.reviewQueryPort = reviewQueryPort;
        this.shopSearchQueryPort = shopSearchQueryPort;
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
    }

    @Override
    public List<PopularKeywordResult> getPopularKeywords() {
        return searchQueryPort.findVisiblePopularKeywords();
    }

    @Override
    public List<RecommendedKeywordResult> getRecommendedKeywords() {
        return searchQueryPort.findVisibleRecommendedKeywords();
    }

    @Override
    public PageResult<SearchProductItemResult> searchMenus(String query, int page, int size) {
        String keyword = validateKeyword(query);
        return productQueryService.searchByKeyword(keyword, page, size);
    }

    @Override
    public PageResult<SearchReviewItemResult> searchReviews(String query, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewQueryPort.searchByKeyword(keyword, pageQuery);
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> searchShopsPaged(String query, Long memberId, int page, int size) {
        String keyword = validateKeyword(query);
        PageQuery pageQuery = PageQuery.of(page, size);
        Long deliveryAdminDongId = memberDeliveryAddressQueryPort
            .findDefaultAdminDongId(MemberId.of(memberId))
            .orElse(null);
        return shopSearchQueryPort.searchByKeywordWithBookmark(keyword, memberId, deliveryAdminDongId, pageQuery);
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
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_BLANK);
        }
        return keyword;
    }

}
