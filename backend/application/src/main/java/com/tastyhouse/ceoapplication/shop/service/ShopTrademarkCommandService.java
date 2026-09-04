package com.tastyhouse.ceoapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.service.ShopImageApprovalService;
import com.tastyhouse.ceoapplication.file.service.FileUploadCommandService;
import com.tastyhouse.ceoapplication.shop.port.in.ShopThumbnailChangeRequestCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopTrademarkChangeRequestCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopTrademarkCommandUseCase;

/**
 * 점주용 가게 상표/대표이미지 변경요청 서비스(CQRS command 측).
 *
 * <p>PENDING 중복 요청 차단 불변식과 변경이력({@code TRADEMARK_CHANGE_REQUEST}·
 * {@code THUMBNAIL_CHANGE_REQUEST}) 기록은 도메인 서비스 {@link ShopImageApprovalService}가 담당하고,
 * 이미지 규격 검증(형식·용량·해상도·비율)은 presentation의 {@link ShopImageSpecValidator}가
 * 업로드 전에 수행한다(core는 fileId만 받는다).
 */
@Service
@Transactional
public class ShopTrademarkCommandService implements ShopTrademarkCommandUseCase {

    private final ShopImageApprovalService shopImageApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileUploadCommandService fileUploadCommandService;

    public ShopTrademarkCommandService(
        ShopImageApprovalService shopImageApprovalService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileUploadCommandService fileUploadCommandService
    ) {
        this.shopImageApprovalService = shopImageApprovalService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    @Override
    public Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateTrademark(file);

        Long imageFileId = fileUploadCommandService.upload(file);
        return shopImageApprovalService.requestImageChange(
            shopId, ShopImageType.TRADEMARK, imageFileId, ShopChangeActor.ceo(ceoId)
        );
    }

    @Override
    public Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateContentImage(file, false);

        Long imageFileId = fileUploadCommandService.upload(file);
        return shopImageApprovalService.requestImageChange(
            shopId, ShopImageType.THUMBNAIL, imageFileId, ShopChangeActor.ceo(ceoId)
        );
    }
}
