package com.tastyhouse.adminapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopNoticeManagementListItemResult;
import com.tastyhouse.application.shop.port.out.ShopNoticeManagementQueryPort;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.shop.response.ShopNoticeManagementListItemResponse;
import com.tastyhouse.adminapplication.shop.port.in.ShopNoticeQueryUseCase;

/**
 * admin용 점주 공지 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 가게 공지를 가게·가게명·게시중단 여부로 필터해 조회한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopNoticeQueryService implements ShopNoticeQueryUseCase {

    private final ShopNoticeManagementQueryPort shopNoticeManagementQueryPort;

    public ShopNoticeQueryService(ShopNoticeManagementQueryPort shopNoticeManagementQueryPort) {
        this.shopNoticeManagementQueryPort = shopNoticeManagementQueryPort;
    }

    @Override
    public PaginationResponse<ShopNoticeManagementListItemResponse> getNotices(
        Long shopId,
        String shopName,
        Boolean hidden,
        int page,
        int size
    ) {
        PageResult<ShopNoticeManagementListItemResult> pageResult = shopNoticeManagementQueryPort
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
