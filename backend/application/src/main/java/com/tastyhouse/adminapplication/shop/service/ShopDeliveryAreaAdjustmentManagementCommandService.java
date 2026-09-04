package com.tastyhouse.adminapplication.shop.service;

import com.tastyhouse.adminapplication.shop.port.in.ShopDeliveryAreaAdjustmentManagementCommandUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopDeliveryAreaAdjustmentRejectCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopDeliveryAreaAdjustmentStatusChangeCommand;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaAdjustmentService;

/**
 * admin용 배달지역 조정 신청 검수 변경 서비스(CQRS command 측).
 *
 * <p>상태 전이 가능 여부(PENDING에서만 조정 개시, IN_PROGRESS에서만 완료)는 도메인 서비스
 * {@link ShopDeliveryAreaAdjustmentService}와 애그리거트가 판정하고, 이 서비스는 HTTP 경계의 상태
 * 문자열을 도메인 enum으로 승격해 해당 전이 메서드로 분기하는 일만 한다.
 *
 * <p><b>완료 처리는 배달가능지역을 반영하지 않는다.</b> 플랫폼은 조정 성립 사실만 기록하고 실제
 * 배달가능지역 변경은 점주가 기존 배달가능지역 API로 별도 수행한다(애그리거트 Javadoc 참조).
 */
@Service
@Transactional
public class ShopDeliveryAreaAdjustmentManagementCommandService implements ShopDeliveryAreaAdjustmentManagementCommandUseCase {

    private final ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService;

    public ShopDeliveryAreaAdjustmentManagementCommandService(ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService) {
        this.shopDeliveryAreaAdjustmentService = shopDeliveryAreaAdjustmentService;
    }

    /**
     * 신청 상태를 전이한다. 반려는 사유가 필요해 별도 엔드포인트로 분리했으므로, 이 경로가 받는 상태는
     * {@code IN_PROGRESS}·{@code COMPLETED} 둘뿐이다 — 그 외 값은 enum으로는 해석되더라도 이 경로에서
     * 지원하지 않는 전이이므로 {@code DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN}(400)으로 거절한다.
     */
    @Override
    public void changeStatus(ShopDeliveryAreaAdjustmentStatusChangeCommand command) {
        Long requestId = command.requestId();
        String status = command.status();
        DeliveryAreaAdjustmentStatus targetStatus = DeliveryAreaAdjustmentStatus.from(status);

        switch (targetStatus) {
            case IN_PROGRESS -> shopDeliveryAreaAdjustmentService.startProgress(requestId);
            case COMPLETED -> shopDeliveryAreaAdjustmentService.complete(requestId);
            default -> throw new BusinessException(ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN,
                ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + status);
        }
    }

    @Override
    public void rejectAdjustment(ShopDeliveryAreaAdjustmentRejectCommand command) {
        Long requestId = command.requestId();
        String reason = command.reason();
        shopDeliveryAreaAdjustmentService.reject(requestId, reason);
    }
}
