package com.tastyhouse.adminapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentManagementQueryPort;
import com.tastyhouse.adminapplication.shop.port.in.ShopDeliveryAreaAdjustmentManagementQueryUseCase;

/**
 * admin용 배달지역 조정 신청 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 신청을 상태·가게로 필터해 조회한다. 동의서 URL은 infra query DAO가 조인으로
 * 완성하므로 여기서 파일을 재조회하지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopDeliveryAreaAdjustmentManagementQueryService implements ShopDeliveryAreaAdjustmentManagementQueryUseCase {

    private final ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort;

    public ShopDeliveryAreaAdjustmentManagementQueryService(ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort) {
        this.shopDeliveryAreaAdjustmentManagementQueryPort = shopDeliveryAreaAdjustmentManagementQueryPort;
    }

    @Override
    public PageResult<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(
        String status,
        Long shopId,
        int page,
        int size
    ) {
        DeliveryAreaAdjustmentStatus adjustmentStatus = status == null ? null : DeliveryAreaAdjustmentStatus.from(status);

        return shopDeliveryAreaAdjustmentManagementQueryPort
            .findAdjustmentRequestPage(adjustmentStatus, shopId, PageQuery.of(page, size));
    }

    @Override
    public ShopDeliveryAreaAdjustmentDetailResult getAdjustmentRequest(Long requestId) {
        return shopDeliveryAreaAdjustmentManagementQueryPort.findAdjustmentRequestById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));
    }
}
