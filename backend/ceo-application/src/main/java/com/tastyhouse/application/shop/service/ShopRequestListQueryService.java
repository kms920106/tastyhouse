package com.tastyhouse.application.shop.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopRequestListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRequestSearchCondition;

@Service
@Transactional(readOnly = true)
class ShopRequestListQueryService implements ShopRequestListQueryUseCase {

    private final ShopRequestQueryPort shopRequestQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestListQueryService(
        ShopRequestQueryPort shopRequestQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestQueryPort = shopRequestQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ShopRequestListItemViewResult> getRequests(
        Long ceoId,
        Long shopId,
        String requestType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateDateRange(startDate, endDate);

        String requestTypeFilter = requestType == null ? null : ShopRequestType.from(requestType).name();
        String statusFilter = status == null ? null : ShopRequestStatus.from(status).name();

        ShopRequestSearchCondition condition = ShopRequestSearchCondition.of(
            shopId,
            requestTypeFilter,
            statusFilter,
            startDate,
            endDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return shopRequestQueryPort.findRequestPage(condition, pageQuery)
            .map(this::toListItemViewResult);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ApplicationException(CeoErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
    }

    private ShopRequestListItemViewResult toListItemViewResult(ShopRequestListItemResult row) {
        ShopRequestType requestType = ShopRequestType.valueOf(row.requestType());
        ShopRequestStatus status = ShopRequestStatus.valueOf(row.status());
        return new ShopRequestListItemViewResult(
            row.requestId(),
            requestType.name(),
            requestType.getDescription(),
            row.summary(),
            status.name(),
            status.getDescription(),
            row.rejectReason(),
            requestType.isContractAmending(),
            row.hasAttachment(),
            row.commentCount(),
            row.requestedAt(),
            row.processedAt()
        );
    }
}
