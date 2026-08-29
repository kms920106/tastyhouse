package com.tastyhouse.adminapi.shop.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopNoticeManagementListItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopNoticeQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopNoticeManagementListItemResponse;
import com.tastyhouse.adminapi.shop.application.port.in.ShopNoticeQueryUseCase;

/**
 * admin용 점주 공지 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 가게 공지를 가게·가게명·게시중단 여부로 필터해 조회한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopNoticeQueryService implements ShopNoticeQueryUseCase {

    private final ShopNoticeQueryDao shopNoticeQueryDao;

    public ShopNoticeQueryService(ShopNoticeQueryDao shopNoticeQueryDao) {
        this.shopNoticeQueryDao = shopNoticeQueryDao;
    }

    @Override
    public PaginationResponse<ShopNoticeManagementListItemResponse> getNotices(
        Long shopId,
        String shopName,
        Boolean hidden,
        int page,
        int size
    ) {
        PageResult<ShopNoticeManagementListItemResult> pageResult = shopNoticeQueryDao
            .findNoticePage(shopId, shopName, hidden, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toShopNoticeManagementListItemResponse));
    }

    private ShopNoticeManagementListItemResponse toShopNoticeManagementListItemResponse(ShopNoticeManagementListItemResult dto) {
        return ShopNoticeManagementListItemResponse.of(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.content(),
            dto.imageUrls(),
            dto.exposed(),
            dto.hidden(),
            dto.createdAt()
        );
    }
}
