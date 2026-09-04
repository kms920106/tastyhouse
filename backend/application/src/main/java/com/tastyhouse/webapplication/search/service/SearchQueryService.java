package com.tastyhouse.webapplication.search.service;

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
import com.tastyhouse.webapplication.product.service.ProductQueryService;
import com.tastyhouse.webapplication.search.port.in.SearchQueryUseCase;

/**
 * 검색 조회 서비스.
 *
 * <p>조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. 인기·추천 검색어는 infra read
 * 어댑터({@link SearchQueryPort})를 주입해 조회하고, 결과를 그대로 반환한다 — 표현 계약(Response)
 * 조립은 web-api 컨트롤러의 책임이다.
 *
 * <p>가게·메뉴·리뷰 검색은 다른 도메인(product/review/shop)의 read model에 위임한다. 리뷰·가게는 각
 * 도메인의 infra query DAO({@link ReviewQueryPort}·{@link ShopSearchQueryPort})를 직접 주입하고, 메뉴 검색은
 * 같은 모듈의 {@link ProductQueryService}(내부적으로 product infra query DAO를 소비)에 위임한다 —
 * 상품 검색 결과 조립은 product 도메인 소관이므로 그 QueryService를 재사용한다.
 *
 * <p><b>챕터 10</b>에서 메뉴 검색의 {@code ProductSummaryResponse} 조립이 컨트롤러로 내려갔다 — 이제 이
 * 서비스는 어느 경로에서도 표현 계약을 만들지 않고 읽기 계약만 반환한다.
 */
@Service
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

    /** 비로그인 검색 — 배송지를 알 수 없으므로 배달지역 필터를 걸지 않는다. */
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
