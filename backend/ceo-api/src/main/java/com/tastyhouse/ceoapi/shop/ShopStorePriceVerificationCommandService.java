package com.tastyhouse.ceoapi.shop;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.service.StorePriceVerificationItemSpec;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.apicommon.file.FileService;
import com.tastyhouse.ceoapi.shop.request.ShopStorePriceVerificationItemRequest;

/**
 * 점주용 매장 가격 인증 요청 접수 서비스(CQRS command 측).
 *
 * <p>불변식(검수 중 재요청 차단·대상 비어있음·할인 진행 중 차단·메뉴/가격 행 소유권 대조)은 도메인
 * {@link StorePriceVerificationService}가 소유하고, 이 서비스는 트랜잭션 경계·가게 소유권 검증·
 * 가격표 이미지 규격 검증·업로드·{@code items} JSON 파싱·통합 인덱스 기록만 담당한다.
 *
 * <p><b>{@code items}가 JSON 문자열인 것은 요청 형식이 multipart이기 때문이다.</b> 가격표 이미지와 대상
 * 목록은 한 트랜잭션에 함께 들어와야 한다 — 이미지를 먼저 올리고 목록을 뒤에 보내는 2단 요청으로 쪼개면
 * 중간에서 끊긴 요청이 첨부만 있고 대상이 없는 고아 상태로 남고, 관리자 검수 큐에 검수할 수 없는 건이
 * 쌓인다. multipart는 JSON 바디를 함께 실을 수 없으므로 목록만 문자열 파트로 받아 여기서 파싱한다.
 *
 * <p><b>인덱스 기록이 도메인이 아니라 이 서비스에 있는 것은 컨텍스트 경계 때문이다.</b> 다른 요청 유형
 * ({@code ShopImageApprovalService}·{@code ShopDeliveryAreaAdjustmentService})은 shop 컨텍스트 소유라
 * 도메인 서비스가 직접 {@link ShopRequestIndexRecorder}를 호출한다. 그러나 인증 요청 애그리거트는
 * <b>product</b> 컨텍스트 소유(승인이 하는 일의 본체가 {@code PRODUCT_PRICE} 갱신이다)여서, 그 도메인
 * 서비스가 {@code shop.service}를 호출하면 {@code ContextBoundaryTest} 위반이 되고 봉인 목록은 늘릴 수
 * 없다. 두 컨텍스트를 한 트랜잭션에서 잇는 일은 표현 계층의 몫이다.
 *
 * <p>{@link MultipartFile}을 파라미터로 받는 것은 파일 업로드 경계의 문서화된 예외다 — 규격 검증이
 * 업로드보다 앞서야 하고, 도메인은 통과분의 {@code fileId}만 받는다.
 */
@Service
@Transactional
public class ShopStorePriceVerificationCommandService {

    private static final TypeReference<List<ShopStorePriceVerificationItemRequest>> ITEMS_TYPE =
        new TypeReference<>() {
        };

    private final StorePriceVerificationService storePriceVerificationService;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final StorePriceListImageSpecValidator storePriceListImageSpecValidator;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    public ShopStorePriceVerificationCommandService(
        StorePriceVerificationService storePriceVerificationService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        ShopOwnershipValidator shopOwnershipValidator,
        StorePriceListImageSpecValidator storePriceListImageSpecValidator,
        FileService fileService,
        ObjectMapper objectMapper
    ) {
        this.storePriceVerificationService = storePriceVerificationService;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.storePriceListImageSpecValidator = storePriceListImageSpecValidator;
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    /**
     * 매장 가격 인증을 요청한다. 규격 통과분만 업로드하므로 규격 미달 파일은 스토리지에 남지 않는다.
     *
     * <p>순서에 의도가 있다 — 소유권 → 목록 파싱 → 규격 검증 → 업로드 → 접수 → 인덱스 기록이다. 파싱을
     * 업로드보다 앞에 두어, 목록이 깨진 요청 때문에 쓸모 없는 파일이 업로드되지 않게 한다.
     *
     * @return 생성된 인증 요청 식별자(검수 대기 상태)
     */
    public Long requestVerification(Long ceoId, Long shopId, MultipartFile file, String items) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<StorePriceVerificationItemSpec> specs = toItemSpecs(items);
        storePriceListImageSpecValidator.validate(file);

        Long priceListFileId = fileService.upload(file);

        ShopId targetShopId = ShopId.of(shopId);
        UploadedFileId targetPriceListFileId = UploadedFileId.of(priceListFileId);
        StorePriceVerification saved =
            storePriceVerificationService.request(targetShopId, targetPriceListFileId, specs, ceoId);

        shopRequestIndexRecorder.record(
            targetShopId,
            ShopRequestType.STORE_PRICE_VERIFICATION,
            saved.getId(),
            describeVerification(specs.size()),
            saved.getPriceListFileId(),
            ceoId
        );
        return saved.getId();
    }

    /**
     * {@code items} 파트를 파싱해 도메인 spec으로 승격한다.
     *
     * <p>파싱 실패를 {@code SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY}(400)로 내리는 것은 이 카탈로그에
     * 범용 400 코드가 없기 때문이다. 목록을 읽을 수 없다는 것은 인증할 대상을 확정할 수 없다는 뜻이라
     * 점주가 화면에서 할 조치(대상을 다시 선택해 보내기)가 빈 목록과 동일하며, 코드가 갈리지 않아
     * 프론트 분기도 하나로 유지된다.
     *
     * <p>{@code applyPickupSamePrice}는 {@code null}을 {@code false}로 접는다 — 체크박스 미전송은
     * "동일 설정 안 함"이고, 켜지 않은 옵션이 승인 시 픽업가를 덮어쓰는 일이 없어야 한다.
     */
    private List<StorePriceVerificationItemSpec> toItemSpecs(String items) {
        List<ShopStorePriceVerificationItemRequest> parsed = parseItems(items);
        return parsed.stream()
            .map(item -> StorePriceVerificationItemSpec.of(
                item.productId(),
                item.priceId(),
                item.storePrice(),
                Boolean.TRUE.equals(item.applyPickupSamePrice())
            ))
            .toList();
    }

    private List<ShopStorePriceVerificationItemRequest> parseItems(String items) {
        if (items == null || items.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY);
        }
        try {
            return objectMapper.readValue(items, ITEMS_TYPE);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY,
                "인증 대상 목록(items)의 형식이 올바르지 않습니다.");
        }
    }

    /**
     * 통합 요청처리 현황 목록에 보일 요약. 관리자가 목록에서 건의 규모를 바로 알 수 있도록 대상 건수를
     * 담는다({@code ShopImageApprovalService#describeImageChangeRequest}와 같은 형태).
     */
    private String describeVerification(int itemCount) {
        return ShopRequestType.STORE_PRICE_VERIFICATION.getDescription() + " (메뉴 " + itemCount + "건)";
    }
}
