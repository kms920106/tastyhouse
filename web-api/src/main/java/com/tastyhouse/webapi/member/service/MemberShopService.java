package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopSearchQueryDao;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;

/**
 * 회원의 즐겨찾기 가게 목록 조회 서비스.
 *
 * <p>CQRS 전환 후 core application 서비스 대신 infra query DAO를 직접 주입한다.
 */
@Service
@RequiredArgsConstructor
public class MemberShopService {

    private final ShopSearchQueryDao shopSearchQueryDao;

    @Transactional(readOnly = true)
    public PageResult<ShopBookmarkListItemResponse> getMyBookmarkedShops(Long memberId, int page, int size) {
        return shopSearchQueryDao.findMyBookmarkedShops(MemberId.of(memberId), PageQuery.of(page, size))
            .map(dto -> ShopBookmarkListItemResponse.from(
                dto.shopId(),
                dto.bookmarkId(),
                dto.shopName(),
                dto.stationName(),
                dto.rating(),
                dto.imageUrl(),
                dto.bookmarked()
            ));
    }
}
