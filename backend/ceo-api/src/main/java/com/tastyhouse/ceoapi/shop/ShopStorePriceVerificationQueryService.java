package com.tastyhouse.ceoapi.shop;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.service.StorePriceUnverifiedItem;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.response.ShopStorePriceUnverifiedItemResponse;
import com.tastyhouse.ceoapi.shop.response.ShopStorePriceVerificationResponse;

/**
 * 점주용 매장 가격 인증 현황 조회 서비스(CQRS query 측).
 *
 * <p>한 번도 요청하지 않은 가게도 <b>404가 아니라 빈 현황</b>을 돌려준다({@code id}·{@code status}·
 * {@code rejectReason}이 null) — 이 응답은 점주 메뉴 화면이 "매장가 입력칸을 열어도 되는가"를 판단하는
 * 근거라, 미요청 가게에서 404가 나면 화면이 정상 상태를 에러로 그린다. 원산지·편의정보 조회가 미등록
 * 가게에 빈 기본값을 주는 것과 같은 판단이다.
 *
 * <p>미인증 메뉴 목록은 요청 이력과 무관하게 <b>항상</b> 계산해 담는다. 요청이 없거나 취소됐어도 점주는
 * "왜 인증이 안 켜지는가"를 알아야 하고, 그 사유는 현재 가격 행의 값만으로 판정되기 때문이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopStorePriceVerificationQueryService {

    private final StorePriceVerificationService storePriceVerificationService;
    private final StorePriceVerificationReader storePriceVerificationReader;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopStorePriceVerificationQueryService(
        StorePriceVerificationService storePriceVerificationService,
        StorePriceVerificationReader storePriceVerificationReader,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.storePriceVerificationService = storePriceVerificationService;
        this.storePriceVerificationReader = storePriceVerificationReader;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public ShopStorePriceVerificationResponse getLatestVerification(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        StorePriceVerification latest = storePriceVerificationReader.readLatest(shopId).orElse(null);
        List<ShopStorePriceUnverifiedItemResponse> unverifiedItems =
            storePriceVerificationService.findUnverifiedItems(ShopId.of(shopId)).stream()
                .map(this::toShopStorePriceUnverifiedItemResponse)
                .toList();

        return ShopStorePriceVerificationResponse.from(
            latest == null ? null : latest.getId(),
            latest == null ? null : latest.getStatus().name(),
            storePriceVerificationReader.readVerified(shopId),
            latest == null ? null : latest.getRejectReason(),
            unverifiedItems
        );
    }

    private ShopStorePriceUnverifiedItemResponse toShopStorePriceUnverifiedItemResponse(StorePriceUnverifiedItem item) {
        return ShopStorePriceUnverifiedItemResponse.from(
            item.productId(),
            item.productName(),
            item.reason().name()
        );
    }
}
