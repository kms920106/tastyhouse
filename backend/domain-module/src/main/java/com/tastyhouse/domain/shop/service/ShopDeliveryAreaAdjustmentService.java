package com.tastyhouse.domain.shop.service;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 프랜차이즈 배달지역 조정 신청 접수·검수(도메인 서비스).
 *
 * <p>담는 불변식은 두 가지다 — (1) 같은 가게에 진행 중(PENDING·IN_PROGRESS) 신청이 있으면 새 신청을
 * 접수하지 않는다(집합 차원 규칙이라 행 하나만 보고는 판정할 수 없다), (2) 상태 전이 가능 여부는
 * 애그리거트가 판정하고 이 서비스는 그 결과를 <b>명시적으로 저장</b>한다(도메인 모델이 POJO라 더티
 * 체킹이 없다).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다. 점주 소유권 검증은 ceo-api 계층
 * ({@code ShopOwnershipValidator})의 책임이라 여기서는 다루지 않는다.
 */
public class ShopDeliveryAreaAdjustmentService {

    /** 새 신청을 막는 "진행 중" 상태 집합 — 종결된 COMPLETED·REJECTED는 재신청을 막지 않는다. */
    private static final List<DeliveryAreaAdjustmentStatus> OPEN_STATUSES =
        List.of(DeliveryAreaAdjustmentStatus.PENDING, DeliveryAreaAdjustmentStatus.IN_PROGRESS);

    private final ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository;

    public ShopDeliveryAreaAdjustmentService(ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository) {
        this.shopDeliveryAreaAdjustmentRequestRepository = shopDeliveryAreaAdjustmentRequestRepository;
    }

    /**
     * 조정 신청을 접수한다.
     *
     * @return 생성된 신청의 식별자
     */
    public Long request(
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId
    ) {
        if (shopDeliveryAreaAdjustmentRequestRepository.existsByShopIdAndStatusIn(shopId, OPEN_STATUSES)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_ALREADY_PENDING);
        }

        ShopDeliveryAreaAdjustmentRequest saved = shopDeliveryAreaAdjustmentRequestRepository.save(
            ShopDeliveryAreaAdjustmentRequest.of(
                shopId,
                counterpartShopName,
                counterpartBusinessNumber,
                franchiseName,
                reason,
                consentFileId
            )
        );
        return saved.getId();
    }

    /**
     * 가맹본부에 자료를 전달해 조정 절차를 시작한다(PENDING → IN_PROGRESS).
     */
    public void startProgress(Long requestId) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.startProgress();
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
    }

    /**
     * 조정 성립을 기록한다(IN_PROGRESS → COMPLETED). 배달가능지역 반영은 별도 수행한다.
     */
    public void complete(Long requestId) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.complete();
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
    }

    /**
     * 신청을 반려한다(PENDING·IN_PROGRESS → REJECTED).
     */
    public void reject(Long requestId, String reason) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.reject(reason);
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
    }

    private ShopDeliveryAreaAdjustmentRequest findRequest(Long requestId) {
        return shopDeliveryAreaAdjustmentRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));
    }
}
