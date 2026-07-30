package com.tastyhouse.ceoapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.domain.shop.domain.service.ShopImageApprovalService;
import com.tastyhouse.ceoapi.file.FileService;

/**
 * 점주용 가게 상표/대표이미지 변경요청 서비스(CQRS command 측).
 *
 * <p>PENDING 중복 요청 차단 불변식은 도메인 서비스 {@link ShopImageApprovalService}가 담당하고,
 * 이미지 규격 검증(형식·용량·해상도·비율)은 presentation의 {@link ShopImageSpecValidator}가
 * 업로드 전에 수행한다(core는 fileId만 받는다).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopTrademarkCommandService {

    private final ShopImageApprovalService shopImageApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileService fileService;

    public Long requestTrademarkChange(Long ceoId, Long shopId, MultipartFile file) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateTrademark(file);

        Long imageFileId = fileService.upload(file);
        return shopImageApprovalService.requestImageChange(shopId, ShopImageType.TRADEMARK, imageFileId);
    }

    public Long requestThumbnailChange(Long ceoId, Long shopId, MultipartFile file) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateContentImage(file, false);

        Long imageFileId = fileService.upload(file);
        return shopImageApprovalService.requestImageChange(shopId, ShopImageType.THUMBNAIL, imageFileId);
    }
}
