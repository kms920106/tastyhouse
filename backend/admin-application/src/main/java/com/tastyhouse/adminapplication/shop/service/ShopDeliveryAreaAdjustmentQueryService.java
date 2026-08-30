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
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.shop.response.ShopDeliveryAreaAdjustmentDetailResponse;
import com.tastyhouse.adminapplication.shop.response.ShopDeliveryAreaAdjustmentListItemResponse;
import com.tastyhouse.adminapplication.shop.port.in.ShopDeliveryAreaAdjustmentQueryUseCase;

/**
 * admin용 배달지역 조정 신청 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 신청을 상태·가게로 필터해 조회한다. 동의서 URL은 infra query DAO가 조인으로
 * 완성하므로 여기서 파일을 재조회하지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class ShopDeliveryAreaAdjustmentQueryService implements ShopDeliveryAreaAdjustmentQueryUseCase {

    private final ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort;

    public ShopDeliveryAreaAdjustmentQueryService(ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort) {
        this.shopDeliveryAreaAdjustmentManagementQueryPort = shopDeliveryAreaAdjustmentManagementQueryPort;
    }

    @Override
    public PaginationResponse<ShopDeliveryAreaAdjustmentListItemResponse> getAdjustmentRequests(
        String status,
        Long shopId,
        int page,
        int size
    ) {
        DeliveryAreaAdjustmentStatus adjustmentStatus = status == null ? null : DeliveryAreaAdjustmentStatus.from(status);

        PageResult<ShopDeliveryAreaAdjustmentListItemResult> pageResult = shopDeliveryAreaAdjustmentManagementQueryPort
            .findAdjustmentRequestPage(adjustmentStatus, shopId, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toShopDeliveryAreaAdjustmentListItemResponse));
    }

    @Override
    public ShopDeliveryAreaAdjustmentDetailResponse getAdjustmentRequest(Long requestId) {
        ShopDeliveryAreaAdjustmentDetailResult dto = shopDeliveryAreaAdjustmentManagementQueryPort.findAdjustmentRequestById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));

        return toShopDeliveryAreaAdjustmentDetailResponse(dto);
    }

    private ShopDeliveryAreaAdjustmentListItemResponse toShopDeliveryAreaAdjustmentListItemResponse(ShopDeliveryAreaAdjustmentListItemResult dto) {
        return ShopDeliveryAreaAdjustmentListItemResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.counterpartShopName(),
            dto.franchiseName(),
            dto.status().name(),
            dto.createdAt()
        );
    }

    private ShopDeliveryAreaAdjustmentDetailResponse toShopDeliveryAreaAdjustmentDetailResponse(ShopDeliveryAreaAdjustmentDetailResult dto) {
        return ShopDeliveryAreaAdjustmentDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.counterpartShopName(),
            dto.counterpartBusinessNumber(),
            dto.franchiseName(),
            dto.reason(),
            dto.consentFileUrl(),
            dto.status().name(),
            dto.rejectReason(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}
