package com.tastyhouse.adminapi.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.query.StorePriceVerificationItemResult;
import com.tastyhouse.infrastructure.product.query.StorePriceVerificationListItemResult;
import com.tastyhouse.infrastructure.product.query.StorePriceVerificationQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.StorePriceVerificationDetailResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.StorePriceVerificationItemResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.StorePriceVerificationListItemResponse;

/**
 * 매장 가격 인증 요청 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 요청을 상태로 필터해 조회한다 — 관리자는 모든 가게의 요청을 본다.
 *
 * <p>가격표 이미지 URL은 infra query DAO가 조인·변환으로 완성하므로 여기서 파일을 재조회하지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class StorePriceVerificationQueryService {

    private final StorePriceVerificationQueryDao storePriceVerificationQueryDao;

    public StorePriceVerificationQueryService(StorePriceVerificationQueryDao storePriceVerificationQueryDao) {
        this.storePriceVerificationQueryDao = storePriceVerificationQueryDao;
    }

    public PaginationResponse<StorePriceVerificationListItemResponse> getVerifications(
        String status,
        int page,
        int size
    ) {
        StorePriceVerificationStatus verificationStatus = promoteStatus(status);

        PageResult<StorePriceVerificationListItemResult> pageResult = storePriceVerificationQueryDao
            .findVerificationPage(verificationStatus, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toStorePriceVerificationListItemResponse));
    }

    /**
     * 인증 요청 상세 — 헤더와 대상 항목을 <b>두 쿼리로</b> 나눠 조회해 조립한다. 한 쿼리로 조인하면
     * 요청 1건이 항목 수만큼 부풀고, 항목 0건인 요청이 조인에서 탈락한다.
     */
    public StorePriceVerificationDetailResponse getVerification(Long verificationId) {
        StorePriceVerificationListItemResult dto = storePriceVerificationQueryDao
            .findVerificationById(verificationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_FOUND));

        List<StorePriceVerificationItemResponse> items = storePriceVerificationQueryDao
            .findVerificationItems(verificationId)
            .stream()
            .map(this::toStorePriceVerificationItemResponse)
            .toList();

        return toStorePriceVerificationDetailResponse(dto, items);
    }

    /** 상태 미지정({@code null})은 "전체"를 뜻하므로 승격하지 않는다. */
    private StorePriceVerificationStatus promoteStatus(String status) {
        return status == null ? null : StorePriceVerificationStatus.from(status);
    }

    private StorePriceVerificationListItemResponse toStorePriceVerificationListItemResponse(
        StorePriceVerificationListItemResult dto
    ) {
        return StorePriceVerificationListItemResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.status().name(),
            dto.priceListFileUrl(),
            dto.rejectReason(),
            dto.itemCount(),
            dto.requestedAt(),
            dto.processedAt()
        );
    }

    private StorePriceVerificationDetailResponse toStorePriceVerificationDetailResponse(
        StorePriceVerificationListItemResult dto,
        List<StorePriceVerificationItemResponse> items
    ) {
        return StorePriceVerificationDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.status().name(),
            dto.priceListFileUrl(),
            dto.rejectReason(),
            dto.requestedAt(),
            dto.processedAt(),
            items
        );
    }

    private StorePriceVerificationItemResponse toStorePriceVerificationItemResponse(
        StorePriceVerificationItemResult dto
    ) {
        return StorePriceVerificationItemResponse.from(
            dto.productId(),
            dto.productName(),
            dto.priceId(),
            dto.priceName(),
            dto.storePrice(),
            dto.deliveryPrice(),
            dto.applyPickupSamePrice()
        );
    }
}
