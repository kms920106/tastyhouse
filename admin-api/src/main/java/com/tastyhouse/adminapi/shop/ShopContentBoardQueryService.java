package com.tastyhouse.adminapi.shop;

import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopContentBoardResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.file.FileService;
import com.tastyhouse.adminapi.shop.response.ShopContentBoardListItemResponse;

/**
 * admin용 가게 콘텐츠보드 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 가게 콘텐츠보드를 가게·숨김여부·콘텐츠 유형으로 필터해 조회한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopContentBoardQueryService {

    private final ShopQueryDao shopQueryDao;
    private final FileService fileService;

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

        // 목록 항목마다 이미지 URL을 단건 조회하면 항목 수만큼 쿼리가 나가므로(N+1), 파일 식별자를 모아
        // 한 번에 변환한 뒤 매핑한다.
        Map<Long, String> imageUrls = fileService.getUrlsByFileIds(
            pageResult.content().stream()
                .map(ShopContentBoardResult::imageFileId)
                .filter(Objects::nonNull)
                .toList()
        );

        return PaginationResponse.from(pageResult.map(dto -> toShopContentBoardListItemResponse(dto, imageUrls)));
    }

    private ShopContentBoardListItemResponse toShopContentBoardListItemResponse(
        ShopContentBoardResult dto,
        Map<Long, String> imageUrls
    ) {
        return ShopContentBoardListItemResponse.of(
            dto.id(),
            dto.shopId(),
            dto.contentType().name(),
            dto.topic().name(),
            dto.imageFileId() == null ? null : imageUrls.get(dto.imageFileId()),
            dto.youtubeUrl(),
            dto.description(),
            dto.hidden(),
            dto.createdAt()
        );
    }
}
