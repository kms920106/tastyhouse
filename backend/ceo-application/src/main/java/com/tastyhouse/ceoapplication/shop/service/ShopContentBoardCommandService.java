package com.tastyhouse.ceoapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.repository.ShopContentBoardRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.apicommon.file.FileService;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopContentBoardUpdateCommand;

/**
 * 점주용 가게 콘텐츠보드 변경 서비스(CQRS command 측).
 *
 * <p>콘텐츠보드는 단일 애그리거트 연산(등록 개수 제한만 검증)이라 도메인 서비스로 하강하지 않고 이
 * 서비스가 write 포트로 직접 다룬다. 이미지 규격 검증은 presentation의
 * {@link ShopImageSpecValidator}가 업로드 전에 수행한다(core는 fileId만 받는다).
 *
 * <p><b>변경이력({@code CONTENT_BOARD})을 예외적으로 이 서비스가 남긴다.</b> 대응 도메인 서비스가 없고
 * 이 서비스가 {@link #loadOwnedContentBoard}로 <b>이미 애그리거트를 손에 들고</b> 있어 변경 전 값을 추가
 * 조회 없이 볼 수 있다({@code ShopMinOrderAmountCommandService}와 같은 형태). 기록만을 위해 도메인
 * 서비스를 새로 만들면 불변식이 없는 껍데기가 하나 늘어난다.
 *
 * <p>등록/수정/삭제가 각각 화면상 별개 조작이므로 <b>행 단위</b>로 {@code CREATE}/{@code UPDATE}/
 * {@code DELETE}를 남긴다.
 */
@Service
@Transactional
public class ShopContentBoardCommandService implements ShopContentBoardCommandUseCase {

    /**
     * 가게당 콘텐츠보드 등록 허용 건수.
     */
    private static final long MAX_CONTENT_BOARD_COUNT = 4;

    private final ShopContentBoardRepository shopContentBoardRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileService fileService;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopContentBoardCommandService(
        ShopContentBoardRepository shopContentBoardRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileService fileService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopContentBoardRepository = shopContentBoardRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileService = fileService;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public Long createContentBoard(ShopContentBoardCreateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String contentType = command.contentType();
        String topic = command.topic();
        String youtubeUrl = command.youtubeUrl();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        if (shopContentBoardRepository.countByShopId(shopId) >= MAX_CONTENT_BOARD_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_BOARD_LIMIT_EXCEEDED);
        }

        ShopContentType type = ShopContentType.from(contentType);
        UploadedFileId imageFileId = uploadIfImage(type, file);

        ShopContentBoard shopContentBoard = ShopContentBoard.of(
            ShopId.of(shopId), type, ShopContentTopic.from(topic), imageFileId, youtubeUrl, description
        );
        ShopContentBoard saved = shopContentBoardRepository.save(shopContentBoard);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONTENT_BOARD,
            ShopChangeActionType.CREATE,
            ShopChangeActor.ceo(ceoId),
            null,
            describeContentBoard(saved)
        );
        return saved.getId();
    }

    @Override
    public void updateContentBoard(ShopContentBoardUpdateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long contentBoardId = command.contentBoardId();
        String topic = command.topic();
        String youtubeUrl = command.youtubeUrl();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopContentBoard shopContentBoard = loadOwnedContentBoard(shopId, contentBoardId);
        // 변경 전 요약을 update 호출 전에 확정한다 — 같은 인스턴스를 제자리에서 갱신하므로
        // 나중에 읽으면 이미 변경 후 값이다.
        String previousValue = describeContentBoard(shopContentBoard);

        UploadedFileId imageFileId = file != null && !file.isEmpty()
            ? uploadIfImage(shopContentBoard.getContentType(), file)
            : shopContentBoard.getImageFileId();

        shopContentBoard.update(ShopContentTopic.from(topic), imageFileId, youtubeUrl, description);
        shopContentBoardRepository.save(shopContentBoard);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONTENT_BOARD,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeContentBoard(shopContentBoard)
        );
    }

    @Override
    public void deleteContentBoard(ShopContentBoardDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long contentBoardId = command.contentBoardId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopContentBoard shopContentBoard = loadOwnedContentBoard(shopId, contentBoardId);
        String previousValue = describeContentBoard(shopContentBoard);

        shopContentBoardRepository.deleteById(contentBoardId);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONTENT_BOARD,
            ShopChangeActionType.DELETE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            null
        );
    }

    /**
     * 콘텐츠보드 1행을 한 줄로 요약한다(예: {@code "가게 소식/이미지: 신메뉴 출시했습니다"}).
     *
     * <p>주제와 콘텐츠 형식을 함께 적는다 — 한 가게에 최대 4건이 공존하므로 주제만으로는 이력 목록에서
     * 어느 행이 바뀐 것인지 특정되지 않는다. 설명 문구는 자르지 않고 원문 그대로 담고, 비어 있으면
     * 영상 URL로 폴백한다(영상 콘텐츠는 설명 없이 등록될 수 있다).
     */
    private String describeContentBoard(ShopContentBoard shopContentBoard) {
        String label = shopContentBoard.getTopic().getDescription()
            + "/" + shopContentBoard.getContentType().getDescription();
        String body = shopContentBoard.getDescription();
        if (body == null || body.isBlank()) {
            body = shopContentBoard.getYoutubeUrl();
        }
        return body == null || body.isBlank() ? label : label + ": " + body;
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
