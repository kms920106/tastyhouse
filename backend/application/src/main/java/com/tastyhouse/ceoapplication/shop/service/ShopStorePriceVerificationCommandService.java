package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.service.StorePriceVerificationItemSpec;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.ceoapplication.file.service.FileUploadOwnerCommandService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tastyhouse.ceoapplication.shop.port.in.ShopStorePriceVerificationCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopStorePriceVerificationItemCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopStorePriceVerificationRequestCommand;

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
public class ShopStorePriceVerificationCommandService implements ShopStorePriceVerificationCommandUseCase {

    private final StorePriceVerificationService storePriceVerificationService;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;
    private static final TypeReference<List<ShopStorePriceVerificationItemCommand>> ITEMS_TYPE =
        new TypeReference<>() {
        };

    private final ObjectMapper objectMapper;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final StorePriceListImageSpecValidator storePriceListImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ShopStorePriceVerificationCommandService(
        ObjectMapper objectMapper,
        StorePriceVerificationService storePriceVerificationService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        ShopOwnershipValidator shopOwnershipValidator,
        StorePriceListImageSpecValidator storePriceListImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.objectMapper = objectMapper;
        this.storePriceVerificationService = storePriceVerificationService;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.storePriceListImageSpecValidator = storePriceListImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    /**
     * 매장 가격 인증을 요청한다. 규격 통과분만 업로드하므로 규격 미달 파일은 스토리지에 남지 않는다.
     *
     * <p>순서에 의도가 있다 — 소유권 → 목록 파싱 → 규격 검증 → 업로드 → 접수 → 인덱스 기록이다. 파싱을
     * 업로드보다 앞에 두어, 목록이 깨진 요청 때문에 쓸모 없는 파일이 업로드되지 않게 한다.
     *
     * @return 생성된 인증 요청 식별자(검수 대기 상태)
     */
    @Override
    public Long requestVerification(ShopStorePriceVerificationRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<StorePriceVerificationItemSpec> specs = toItemSpecs(command.items());
        storePriceListImageSpecValidator.validate(file);

        Long priceListFileId = fileUploadCommandService.upload(file);

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
     * command의 인증 대상 목록을 도메인 spec으로 승격한다.
     *
     * <p>{@code items} 파트는 JSON 배열 문자열로 와서 여기서 command 목록으로 파싱된다 — 서비스가
     * {@code ..request..}를 알 수 없고(챕터 02 §5) 컨트롤러·Request record는 domain-free라
     * {@code BusinessException}을 던질 수 없어, 파싱을 여기 두는 것이 세 규칙을 모두 만족하는 형태다.
     * 파싱 실패와 빈 목록을 같은 {@code SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY}(400)로 내리던
     * 계약은 그대로다.
     *
     * <p>{@code applyPickupSamePrice}는 {@code null}을 {@code false}로 접는다 — 체크박스 미전송은
     * "동일 설정 안 함"이고, 켜지 않은 옵션이 승인 시 픽업가를 덮어쓰는 일이 없어야 한다.
     */
    private List<StorePriceVerificationItemSpec> toItemSpecs(String items) {
        return parseItems(items).stream()
            .map(item -> StorePriceVerificationItemSpec.of(
                item.productId(),
                item.priceId(),
                item.storePrice(),
                Boolean.TRUE.equals(item.applyPickupSamePrice())
            ))
            .toList();
    }

    private List<ShopStorePriceVerificationItemCommand> parseItems(String items) {
        if (items == null || items.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY);
        }
        List<ShopStorePriceVerificationItemCommand> parsed;
        try {
            parsed = objectMapper.readValue(items, ITEMS_TYPE);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY,
                "인증 대상 목록(items)의 형식이 올바르지 않습니다.");
        }
        if (parsed == null || parsed.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY);
        }
        return parsed;
    }

    /**
     * 통합 요청처리 현황 목록에 보일 요약. 관리자가 목록에서 건의 규모를 바로 알 수 있도록 대상 건수를
     * 담는다({@code ShopImageApprovalService#describeImageChangeRequest}와 같은 형태).
     */
    private String describeVerification(int itemCount) {
        return ShopRequestType.STORE_PRICE_VERIFICATION.getDescription() + " (메뉴 " + itemCount + "건)";
    }
}
