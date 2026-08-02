package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.repository.ShopContentBoardRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.apicommon.file.FileService;

/**
 * 점주용 가게 콘텐츠보드 변경 서비스(CQRS command 측).
 *
 * <p>콘텐츠보드는 단일 애그리거트 연산(등록 개수 제한만 검증)이라 도메인 서비스로 하강하지 않고 이
 * 서비스가 write 포트로 직접 다룬다. 이미지 규격 검증은 presentation의
 * {@link ShopImageSpecValidator}가 업로드 전에 수행한다(core는 fileId만 받는다).
 */
@Service
@Transactional
public class ShopContentBoardCommandService {

    /**
     * 가게당 콘텐츠보드 등록 허용 건수.
     */
    private static final long MAX_CONTENT_BOARD_COUNT = 4;

    private final ShopContentBoardRepository shopContentBoardRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileService fileService;

    public ShopContentBoardCommandService(
        ShopContentBoardRepository shopContentBoardRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileService fileService
    ) {
        this.shopContentBoardRepository = shopContentBoardRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileService = fileService;
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

        if (shopContentBoardRepository.countByShopId(shopId) >= MAX_CONTENT_BOARD_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_BOARD_LIMIT_EXCEEDED);
        }

        ShopContentType type = ShopContentType.from(contentType);
        UploadedFileId imageFileId = uploadIfImage(type, file);

        ShopContentBoard shopContentBoard = ShopContentBoard.of(
            ShopId.of(shopId), type, ShopContentTopic.from(topic), imageFileId, youtubeUrl, description
        );
        return shopContentBoardRepository.save(shopContentBoard).getId();
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

        ShopContentBoard shopContentBoard = loadOwnedContentBoard(shopId, contentBoardId);
        UploadedFileId imageFileId = file != null && !file.isEmpty()
            ? uploadIfImage(shopContentBoard.getContentType(), file)
            : shopContentBoard.getImageFileId();

        shopContentBoard.update(ShopContentTopic.from(topic), imageFileId, youtubeUrl, description);
        shopContentBoardRepository.save(shopContentBoard);
    }

    public void deleteContentBoard(Long ceoId, Long shopId, Long contentBoardId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        loadOwnedContentBoard(shopId, contentBoardId);
        shopContentBoardRepository.deleteById(contentBoardId);
    }

    /**
     * 콘텐츠보드를 로드하고 그것이 대상 가게 소속인지 확인한다.
     */
    private ShopContentBoard loadOwnedContentBoard(Long shopId, Long contentBoardId) {
        ShopContentBoard shopContentBoard = shopContentBoardRepository.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
        if (!shopContentBoard.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND);
        }
        return shopContentBoard;
    }

    /**
     * 영상 콘텐츠는 파일 업로드가 없고, 이미지·GIF는 규격 검증 후 업로드한다.
     */
    private UploadedFileId uploadIfImage(ShopContentType contentType, MultipartFile file) {
        if (contentType == ShopContentType.VIDEO) {
            return null;
        }
        shopImageSpecValidator.validateContentImage(file, contentType == ShopContentType.GIF);
        return UploadedFileId.of(fileService.upload(file));
    }
}
