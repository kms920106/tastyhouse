package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentTopic;
import com.tastyhouse.core.domain.shop.application.ShopContentBoardCommandService;
import com.tastyhouse.core.domain.shop.application.ShopContentBoardQueryService;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopContentBoardCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopContentBoardUpdateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopContentBoardResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.ceoapi.file.FileService;
import com.tastyhouse.ceoapi.shop.response.ShopContentBoardResponse;

/**
 * 점주용 가게 콘텐츠보드 중개 서비스. 모든 조회·등록·수정·삭제는 로그인 점주(ceoId)의
 * 소유 가게로 한정하며, 소유권 검증은 {@link ShopOwnershipValidator}에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class ShopContentBoardService {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopContentBoardCommandService shopContentBoardCommandService;
    private final ShopContentBoardQueryService shopContentBoardQueryService;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileService fileService;

    public List<ShopContentBoardResponse> getContentBoards(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopContentBoardQueryService.findContentBoards(shopId).stream()
            .map(this::toShopContentBoardResponse)
            .toList();
    }

    public Long createContentBoard(
        Long ceoId,
        Long shopId,
        String contentType,
        String topic,
        MultipartFile file,
        String youtubeUrl,
        String description
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopContentType type = ShopContentType.from(contentType);
        Long imageFileId = uploadIfImage(type, file);

        ShopContentBoardCreateCommand command = ShopContentBoardCreateCommand.of(
            shopId, type, ShopContentTopic.from(topic), imageFileId, youtubeUrl, description
        );
        return shopContentBoardCommandService.createContentBoard(command);
    }

    public void updateContentBoard(
        Long ceoId,
        Long shopId,
        Long contentBoardId,
        String topic,
        MultipartFile file,
        String youtubeUrl,
        String description
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopContentBoardResult existing = findExisting(shopId, contentBoardId);
        Long imageFileId = file != null && !file.isEmpty() ? uploadIfImage(existing.contentType(), file) : existing.imageFileId();

        ShopContentBoardUpdateCommand command = ShopContentBoardUpdateCommand.of(
            ShopContentTopic.from(topic), imageFileId, youtubeUrl, description
        );
        shopContentBoardCommandService.updateContentBoard(contentBoardId, command);
    }

    public void deleteContentBoard(Long ceoId, Long shopId, Long contentBoardId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        findExisting(shopId, contentBoardId);
        shopContentBoardCommandService.deleteContentBoard(contentBoardId);
    }

    private ShopContentBoardResult findExisting(Long shopId, Long contentBoardId) {
        return shopContentBoardQueryService.findContentBoards(shopId).stream()
            .filter(result -> result.id().equals(contentBoardId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
    }

    private Long uploadIfImage(ShopContentType contentType, MultipartFile file) {
        if (contentType == ShopContentType.VIDEO) {
            return null;
        }
        shopImageSpecValidator.validateContentImage(file, contentType == ShopContentType.GIF);
        return fileService.upload(file);
    }

    private ShopContentBoardResponse toShopContentBoardResponse(ShopContentBoardResult dto) {
        return ShopContentBoardResponse.of(
            dto.id(),
            dto.shopId(),
            dto.contentType().name(),
            dto.topic().name(),
            dto.imageFileId(),
            dto.youtubeUrl(),
            dto.description(),
            dto.hidden()
        );
    }
}
