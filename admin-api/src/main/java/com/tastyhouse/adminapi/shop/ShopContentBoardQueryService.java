package com.tastyhouse.adminapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
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
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public PaginationResponse<ShopContentBoardListItemResponse> getContentBoards(
        Long shopId,
        Boolean hidden,
        String contentType,
        int page,
        int size
    ) {
        ShopContentType type = contentType == null ? null : ShopContentType.from(contentType);

        PageResult<ShopContentBoardListItemResponse> pageResult = shopQueryDao
            .findContentBoardPage(shopId, hidden, type, PageQuery.of(page, size))
            .map(this::toShopContentBoardListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private ShopContentBoardListItemResponse toShopContentBoardListItemResponse(ShopContentBoardResult dto) {
        return ShopContentBoardListItemResponse.of(
            dto.id(),
            dto.shopId(),
            dto.contentType().name(),
            dto.topic().name(),
            resolveImageUrl(dto.imageFileId()),
            dto.youtubeUrl(),
            dto.description(),
            dto.hidden(),
            dto.createdAt()
        );
    }

    private String resolveImageUrl(Long imageFileId) {
        if (imageFileId == null) {
            return null;
        }
        return fileQueryService.findFilePath(UploadedFileId.of(imageFileId))
            .map(fileService::getUrlByPath)
            .orElse(null);
    }
}
