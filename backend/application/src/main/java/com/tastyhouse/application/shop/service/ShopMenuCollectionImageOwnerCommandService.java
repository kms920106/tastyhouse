package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.service.ShopMenuCollectionImageService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageReorderCommand;

/**
 * 점주용 메뉴모음컷 변경 서비스(CQRS command 측).
 *
 * <p>불변식(최대 6개·최소 1개 유지·순서 replace-all·검수 상태 전이)은 도메인 서비스
 * {@link ShopMenuCollectionImageService}가 소유하고, 이 서비스는 트랜잭션 경계·소유권 검증·경계 타입
 * 승격(Long → ID VO)과 <b>업로드 전 규격 검증</b>만 담당한다.
 *
 * <p>{@link MultipartFile}을 파라미터로 받는 것은 파일 업로드 경계의 문서화된 예외다 — 규격 검증이
 * 업로드보다 앞서야 하고({@link ShopMenuCollectionImageSpecValidator}), {@code domain}은 통과분의
 * {@code fileId}만 받는다.
 *
 * <p><b>삭제는 경로의 {@code shopId}로 범위를 좁혀 수행한다.</b> 도메인 서비스가 가게의 목록을 읽어
 * 그 안에서 대상을 찾으므로, 남의 가게 이미지 id를 넣으면 소유권 검증(가게)을 통과했더라도
 * {@code SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND}로 떨어진다 — 이미지 → 가게 역조회를 별도로 짜지 않고도
 * IDOR이 닫힌다.
 */
@Service
@CeoApp
@Transactional
public class ShopMenuCollectionImageOwnerCommandService implements ShopMenuCollectionImageOwnerCommandUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopMenuCollectionImageSpecValidator shopMenuCollectionImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ShopMenuCollectionImageOwnerCommandService(
        ShopMenuCollectionImageService shopMenuCollectionImageService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopMenuCollectionImageSpecValidator shopMenuCollectionImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopMenuCollectionImageSpecValidator = shopMenuCollectionImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    /**
     * 메뉴모음컷을 등록한다. 규격 통과분만 업로드하므로 규격 미달 파일은 스토리지에 남지 않는다.
     *
     * @return 생성된 메뉴모음컷 식별자(검수 대기 상태)
     */
    @Override
    public Long registerMenuCollectionImage(ShopMenuCollectionImageCreateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopMenuCollectionImageSpecValidator.validate(file);

        Long imageFileId = fileUploadCommandService.upload(file);
        ShopId id = ShopId.of(shopId);
        return shopMenuCollectionImageService.register(id, UploadedFileId.of(imageFileId));
    }

    /**
     * 표시 순서를 통째로 교체한다. <b>승인을 거치지 않는다</b> — 검수 대상은 새 이미지의 내용이지
     * 배치가 아니다.
     */
    @Override
    public void reorderMenuCollectionImages(ShopMenuCollectionImageReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> imageIds = command.imageIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId id = ShopId.of(shopId);
        shopMenuCollectionImageService.reorder(id, imageIds);
    }

    /**
     * 메뉴모음컷을 삭제한다. 순서 변경과 마찬가지로 승인을 거치지 않지만, <b>최소 1개는 남아야 한다</b> —
     * 0개가 되면 손님이 가게를 열었을 때 최상단이 빈 채로 노출된다.
     */
    @Override
    public void deleteMenuCollectionImage(ShopMenuCollectionImageDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long imageId = command.imageId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId id = ShopId.of(shopId);
        ShopMenuCollectionImageId menuCollectionImageId = ShopMenuCollectionImageId.of(imageId);
        shopMenuCollectionImageService.delete(id, menuCollectionImageId);
    }
}
