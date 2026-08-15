package com.tastyhouse.ceoapi.shop;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.domain.shop.repository.ShopNoticeImageRepository;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopNoticeExposureService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.apicommon.file.FileService;

/**
 * 점주용 가게 공지(사장님 공지) 변경 서비스(CQRS command 측).
 *
 * <p>공지 본문은 단일 애그리거트 연산이라 write 포트로 직접 다루지만, <b>앱 노출 토글만은</b> "가게당 1건"
 * 집합 불변식이라 도메인 서비스 {@link ShopNoticeExposureService}에 위임한다. 이미지 규격 검증은
 * presentation의 {@link ShopImageSpecValidator}가 업로드 전에 수행한다(core는 fileId만 받는다).
 *
 * <p><b>이미지 교체는 replace-all이다.</b> 3장 제한·정렬순서가 집합 규칙이라 개별 행 CRUD를 열면 중간
 * 상태가 규칙을 위반하기 때문이다.
 *
 * <p>변경이력({@code NOTICE})은 {@code ShopContentBoardCommandService}와 동형으로 이 서비스가 남긴다 —
 * 대응 도메인 서비스가 노출 토글만 담당하고, 이 서비스가 {@link #loadOwnedNotice}로 이미 애그리거트를
 * 손에 들고 있어 변경 전 값을 추가 조회 없이 볼 수 있다. 등록/수정/삭제/노출토글이 각각 화면상 별개
 * 조작이므로 <b>행 단위</b>로 {@code CREATE}/{@code UPDATE}/{@code DELETE}를 남긴다.
 */
@Service
@Transactional
public class ShopNoticeCommandService {

    /**
     * 공지 1건에 첨부할 수 있는 이미지 수.
     */
    private static final int MAX_NOTICE_IMAGE_COUNT = 3;

    private final ShopNoticeRepository shopNoticeRepository;
    private final ShopNoticeImageRepository shopNoticeImageRepository;
    private final ShopNoticeExposureService shopNoticeExposureService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final FileService fileService;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeCommandService(
        ShopNoticeRepository shopNoticeRepository,
        ShopNoticeImageRepository shopNoticeImageRepository,
        ShopNoticeExposureService shopNoticeExposureService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        ProhibitedWordValidator prohibitedWordValidator,
        FileService fileService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticeRepository = shopNoticeRepository;
        this.shopNoticeImageRepository = shopNoticeImageRepository;
        this.shopNoticeExposureService = shopNoticeExposureService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.fileService = fileService;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public Long createNotice(Long ceoId, Long shopId, String content, List<MultipartFile> files, Boolean exposed) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(content);

        List<MultipartFile> images = normalizeFiles(files);
        validateImageCount(images);

        ShopNotice saved = shopNoticeRepository.save(ShopNotice.of(ShopId.of(shopId), content));
        saveImages(saved.getId(), images);

        if (Boolean.TRUE.equals(exposed)) {
            shopNoticeExposureService.expose(ShopId.of(shopId), saved);
        }

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.CREATE,
            ShopChangeActor.ceo(ceoId),
            null,
            describeNotice(saved)
        );
        return saved.getId();
    }

    public void updateNotice(
        Long ceoId,
        Long shopId,
        Long noticeId,
        String content,
        List<MultipartFile> files,
        Boolean keepExistingImages
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(content);

        ShopNotice notice = loadOwnedNotice(shopId, noticeId);
        // 변경 전 요약을 updateContent 호출 전에 확정한다 — 같은 인스턴스를 제자리에서 갱신하므로
        // 나중에 읽으면 이미 변경 후 값이다.
        String previousValue = describeNotice(notice);

        if (!Boolean.TRUE.equals(keepExistingImages)) {
            List<MultipartFile> images = normalizeFiles(files);
            validateImageCount(images);
            shopNoticeImageRepository.deleteByShopNoticeId(noticeId);
            saveImages(noticeId, images);
        }

        notice.updateContent(content);
        shopNoticeRepository.save(notice);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeNotice(notice)
        );
    }

    public void deleteNotice(Long ceoId, Long shopId, Long noticeId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopNotice notice = loadOwnedNotice(shopId, noticeId);
        String previousValue = describeNotice(notice);

        // 업로드된 파일(UPLOADED_FILE)은 삭제하지 않는다 — 첨부 이력 보존 정책.
        shopNoticeImageRepository.deleteByShopNoticeId(noticeId);
        shopNoticeRepository.deleteById(noticeId);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.DELETE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            null
        );
    }

    /**
     * 앱 노출을 토글한다. 켜는 경우 같은 가게의 기존 노출 공지는 도메인 서비스가 함께 내린다.
     *
     * <p>{@code hidden = true}인 공지도 토글 자체는 허용한다 — 게시중단이 풀리면 점주 의도대로 노출되어야
     * 하기 때문이다(그동안 web에는 나오지 않는다).
     */
    public void changeExposure(Long ceoId, Long shopId, Long noticeId, boolean exposed) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopNotice notice = loadOwnedNotice(shopId, noticeId);
        String previousValue = describeNotice(notice);

        if (exposed) {
            shopNoticeExposureService.expose(ShopId.of(shopId), notice);
        } else {
            shopNoticeExposureService.unexpose(notice);
        }

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeNotice(notice)
        );
    }

    /**
     * 공지 1건을 한 줄로 요약한다(예: {@code "노출중: 이번 주 신메뉴 출시했습니다"}).
     *
     * <p>노출 상태를 함께 적는다 — 노출 토글도 {@code UPDATE}로 기록되므로 본문만으로는 이력 목록에서
     * 무엇이 바뀐 것인지 구분되지 않는다.
     */
    private String describeNotice(ShopNotice notice) {
        String label = notice.isExposed() ? "노출중" : "미노출";
        return label + ": " + notice.getContent();
    }

    /**
     * 공지를 로드하고 그것이 대상 가게 소속인지 확인한다.
     */
    private ShopNotice loadOwnedNotice(Long shopId, Long noticeId) {
        ShopNotice notice = shopNoticeRepository.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOTICE_NOT_FOUND));
        if (!notice.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_NOTICE_NOT_FOUND);
        }
        return notice;
    }

    /**
     * 규격 검증을 통과한 이미지를 업로드해 요청 배열 순서대로 {@code sortOrder}를 매긴다.
     *
     * <p><b>전량 검증을 먼저 끝낸 뒤에 업로드한다.</b> 파일 단위로 검증·업로드를 교차하면, 뒤쪽 파일이
     * 규격 위반일 때 앞쪽 파일은 이미 외부 스토리지에 올라간 상태가 된다. 트랜잭션 롤백은
     * {@code UPLOADED_FILE} 행만 되돌리고 스토리지에 쓴 바이트는 되돌리지 못하므로, 실패 시도마다 고아
     * 파일이 누적된다.
     */
    private void saveImages(Long noticeId, List<MultipartFile> images) {
        if (images.isEmpty()) {
            return;
        }

        images.forEach(shopImageSpecValidator::validateNoticeImage);

        List<ShopNoticeImage> noticeImages = new ArrayList<>(images.size());
        for (int sortOrder = 0; sortOrder < images.size(); sortOrder++) {
            MultipartFile file = images.get(sortOrder);
            noticeImages.add(ShopNoticeImage.of(noticeId, UploadedFileId.of(fileService.upload(file)), sortOrder));
        }
        shopNoticeImageRepository.saveAll(noticeImages);
    }

    private void validateImageCount(List<MultipartFile> images) {
        if (images.size() > MAX_NOTICE_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_NOTICE_IMAGE_LIMIT_EXCEEDED);
        }
    }

    /**
     * multipart 요청은 파일 파트가 없으면 null, 빈 파트가 하나 붙어 올 수도 있으므로 둘 다 걸러낸다.
     */
    private List<MultipartFile> normalizeFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
            .filter(file -> file != null && !file.isEmpty())
            .toList();
    }
}
