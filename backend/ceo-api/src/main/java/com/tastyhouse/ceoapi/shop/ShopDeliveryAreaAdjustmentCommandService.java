package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaAdjustmentService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.apicommon.file.FileService;

/**
 * 점주용 배달지역 조정 신청 접수 서비스(CQRS command 측).
 *
 * <p>진행 중 신청 중복 차단 불변식은 도메인 서비스 {@link ShopDeliveryAreaAdjustmentService}가 담당하고,
 * 이 서비스는 소유권 검증·동의서 업로드·트랜잭션 경계·식별자 VO 승격만 책임진다
 * ({@code ShopTrademarkCommandService}와 동일 구조 — 도메인 컨트롤러가 받은 {@code MultipartFile}을
 * {@link FileService}로 업로드한 뒤 fileId만 도메인에 넘긴다).
 *
 * <p><b>변경이력</b>: {@code DELIVERY_AREA_ADJUSTMENT} 기록은 접수 결과를 손에 든
 * {@link ShopDeliveryAreaAdjustmentService}가 담당하고, 이 서비스는 변경 주체
 * ({@link ShopChangeActor})만 만들어 전달한다.
 *
 * <p>동의서는 이미지가 아니라 PDF도 허용해야 하므로 {@code ShopImageSpecValidator}(해상도·비율 검증)를
 * 태우지 않는다. 형식·용량 검증은 도메인 {@code FileUploadService}의 공통 규칙이 담당한다.
 */
@Service
@Transactional
public class ShopDeliveryAreaAdjustmentCommandService {

    private final ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final FileService fileService;

    public ShopDeliveryAreaAdjustmentCommandService(
        ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService,
        ShopOwnershipValidator shopOwnershipValidator,
        FileService fileService
    ) {
        this.shopDeliveryAreaAdjustmentService = shopDeliveryAreaAdjustmentService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.fileService = fileService;
    }

    public Long requestAdjustment(
        Long ceoId,
        Long shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        MultipartFile file
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        Long consentFileId = fileService.upload(file);

        ShopId targetShopId = ShopId.of(shopId);
        UploadedFileId targetConsentFileId = UploadedFileId.of(consentFileId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return shopDeliveryAreaAdjustmentService.request(
            targetShopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            targetConsentFileId,
            actor
        );
    }
}
