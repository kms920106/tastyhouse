package com.tastyhouse.adminapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopContentBoardResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.response.ShopContentBoardListItemResponse;

/**
 * admin용 가게 콘텐츠보드 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 가게 콘텐츠보드를 가게·숨김여부·콘텐츠 유형으로 필터해 조회한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopContentBoardQueryService {

    private final ShopQueryDao shopQueryDao;

    public ShopContentBoardQueryService(ShopQueryDao shopQueryDao) {
        this.shopQueryDao = shopQueryDao;
    }

    public PaginationResponse<ShopContentBoardListItemResponse> getContentBoards(
        Long shopId,
        Boolean hidden,
        String contentType,
        int page,
        int size
    ) {
        ShopContentType type = contentType == null ? null : ShopContentType.from(contentType);

        PageResult<ShopContentBoardResult> pageResult = shopQueryDao
            .findContentBoardPage(shopId, hidden, type, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toShopContentBoardListItemResponse));
    }

    private ShopContentBoardListItemResponse toShopContentBoardListItemResponse(ShopContentBoardResult dto) {
        return ShopContentBoardListItemResponse.of(
            dto.id(),
            dto.shopId(),
            dto.contentType().name(),
            dto.topic().name(),
            dto.imageUrl(),
            dto.youtubeUrl(),
            dto.description(),
            dto.hidden(),
            dto.createdAt()
        );
    }
}
