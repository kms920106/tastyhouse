package com.tastyhouse.domain.shop.service;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.model.ShopRequestType;
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
 * <p><b>변경이력({@code DELIVERY_AREA_ADJUSTMENT})은 신청 접수 시점에만 남긴다.</b> 이후 상태 전이
 * (개시·완료·반려)는 플랫폼 검수 진행 상황이지 <b>가게 설정 변경이 아니므로</b> 가게 변경이력의 대상이
 * 아니다 — 남기면 점주가 바꾼 것과 관리자가 처리한 것이 한 목록에 섞여, 이력이 "가게가 어떻게 설정돼
 * 왔는가"를 답하지 못한다. 신청 진행 상황은 조정 신청 상세 API가 별도로 보여준다.
 *
 * <p><b>요청처리 현황 인덱스({@link ShopRequestIndexRecorder})는 반대로 모든 전이를 기록한다.</b> 변경이력이
 * 접수 시점만 남기는 것과 대비되는데, 그 이력은 "가게가 어떻게 설정돼 왔는가"를 답하고 인덱스는 "내가 낸
 * 신청이 어떻게 처리됐는가"를 답하기 때문이다 — 후자에서는 검수 진행 상황이 곧 본문이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ShopDomainConfig}가 담당한다. 점주 소유권 검증은 ceo-api 계층
 * ({@code ShopOwnershipValidator})의 책임이라 여기서는 다루지 않는다. 변경 주체
 * ({@link ShopChangeActor})는 도메인이 인증을 모르므로 마지막 파라미터로 명시 전달받는다.
 */
public class ShopDeliveryAreaAdjustmentService {

    /** 새 신청을 막는 "진행 중" 상태 집합 — 종결된 COMPLETED·REJECTED는 재신청을 막지 않는다. */
    private static final List<DeliveryAreaAdjustmentStatus> OPEN_STATUSES =
        List.of(DeliveryAreaAdjustmentStatus.PENDING, DeliveryAreaAdjustmentStatus.IN_PROGRESS);

    private final ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopDeliveryAreaAdjustmentService(
        ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopDeliveryAreaAdjustmentRequestRepository = shopDeliveryAreaAdjustmentRequestRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
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
        UploadedFileId consentFileId,
        ShopChangeActor actor
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

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA_ADJUSTMENT,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeAdjustmentRequest(saved)
        );

        shopRequestIndexRecorder.record(
            shopId,
            ShopRequestType.DELIVERY_AREA_ADJUSTMENT,
            saved.getId(),
            describeAdjustmentRequest(saved),
            saved.getConsentFileId(),
            actor.actorId()
        );
        return saved.getId();
    }

    /**
     * 조정 신청 1건을 한 줄로 요약한다(예: {@code "맛있는집 강남점 (BBQ)"}).
     *
     * <p>상대 가게명과 가맹본부명만 담는다 — 신청 사유는 자유 서술이라 길이가 들쭉날쭉해 한 줄 요약에
     * 맞지 않고, 동의서 파일은 이력에서 열 수단이 없다. 둘 다 조정 신청 상세 API가 보여준다.
     */
    private String describeAdjustmentRequest(ShopDeliveryAreaAdjustmentRequest request) {
        return request.getCounterpartShopName() + " (" + request.getFranchiseName() + ")";
    }

    /**
     * 가맹본부에 자료를 전달해 조정 절차를 시작한다(PENDING → IN_PROGRESS).
     */
    public void startProgress(Long requestId) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.startProgress();
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
        shopRequestIndexRecorder.syncAdjustmentStatus(requestId, request.getStatus(), null);
    }

    /**
     * 조정 성립을 기록한다(IN_PROGRESS → COMPLETED). 배달가능지역 반영은 별도 수행한다.
     */
    public void complete(Long requestId) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.complete();
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
        shopRequestIndexRecorder.syncAdjustmentStatus(requestId, request.getStatus(), null);
    }

    /**
     * 신청을 반려한다(PENDING·IN_PROGRESS → REJECTED).
     */
    public void reject(Long requestId, String reason) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.reject(reason);
        shopDeliveryAreaAdjustmentRequestRepository.save(request);
        shopRequestIndexRecorder.syncAdjustmentStatus(requestId, request.getStatus(), reason);
    }

    private ShopDeliveryAreaAdjustmentRequest findRequest(Long requestId) {
        return shopDeliveryAreaAdjustmentRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));
    }
}
