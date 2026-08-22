package com.tastyhouse.adminapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopMenuCollectionImageRequestResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.response.ShopMenuCollectionImageRequestItemResponse;

/**
 * 메뉴모음컷 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 요청을 승인 상태로 필터해 조회한다 — 관리자는 모든 가게의 요청을 본다.
 */
@Service
@Transactional(readOnly = true)
public class ShopMenuCollectionImageQueryService {

    private final ShopQueryDao shopQueryDao;

    public ShopMenuCollectionImageQueryService(ShopQueryDao shopQueryDao) {
        this.shopQueryDao = shopQueryDao;
    }

    public PaginationResponse<ShopMenuCollectionImageRequestItemResponse> getMenuCollectionImageRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        PageResult<ShopMenuCollectionImageRequestResult> pageResult = shopQueryDao
            .findMenuCollectionImageRequestPage(approvalStatus, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toShopMenuCollectionImageRequestItemResponse));
    }

    /** 상태 미지정({@code null})은 "전체"를 뜻하므로 승격하지 않는다. */
    private ApprovalStatus promoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status);
    }

    private ShopMenuCollectionImageRequestItemResponse toShopMenuCollectionImageRequestItemResponse(
        ShopMenuCollectionImageRequestResult dto
    ) {
        return ShopMenuCollectionImageRequestItemResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.imageUrl(),
            dto.sort(),
            dto.status().name(),
            dto.rejectReason()
        );
    }
}
