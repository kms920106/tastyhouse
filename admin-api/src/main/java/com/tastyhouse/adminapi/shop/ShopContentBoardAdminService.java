package com.tastyhouse.adminapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.domain.shop.application.ShopContentBoardCommandService;
import com.tastyhouse.core.domain.shop.application.ShopContentBoardQueryService;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopContentBoardResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.file.FileService;
import com.tastyhouse.adminapi.shop.response.ShopContentBoardListItemResponse;

/**
 * admin용 가게 콘텐츠보드 검수 중개 서비스. 소유권 검증 없이 전체 가게 콘텐츠보드를 목록 조회하고
 * 숨김 처리/삭제한다.
 */
@Service
@RequiredArgsConstructor
public class ShopContentBoardAdminService {

    private final ShopContentBoardCommandService shopContentBoardCommandService;
    private final ShopContentBoardQueryService shopContentBoardQueryService;
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public PaginationResponse<ShopContentBoardListItemResponse> getContentBoards(Long shopId, Boolean hidden, String contentType, int page, int size) {
        ShopContentType type = contentType == null ? null : ShopContentType.from(contentType);

        PageResult<ShopContentBoardListItemResponse> pageResult = shopContentBoardQueryService
            .findContentBoards(shopId, hidden, type, page, size)
            .map(this::toShopContentBoardListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public void changeHidden(Long contentBoardId, boolean hidden) {
        shopContentBoardCommandService.changeHidden(contentBoardId, hidden);
    }

    public void deleteContentBoard(Long contentBoardId) {
        shopContentBoardCommandService.deleteContentBoard(contentBoardId);
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
