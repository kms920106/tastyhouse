package com.tastyhouse.adminapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopImageChangeRequestResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.response.ShopImageChangeRequestItemResponse;

/**
 * admin용 가게 이미지(상표/대표이미지) 변경요청 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 요청을 승인 상태·이미지 유형으로 필터해 조회한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopImageChangeQueryService {

    private final ShopQueryDao shopQueryDao;

    public PaginationResponse<ShopImageChangeRequestItemResponse> getImageChangeRequests(
        String status,
        String imageType,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = status == null ? null : ApprovalStatus.valueOf(status);
        ShopImageType type = imageType == null ? null : ShopImageType.from(imageType);

        PageResult<ShopImageChangeRequestResult> pageResult = shopQueryDao
            .findImageChangeRequestPage(approvalStatus, type, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toShopImageChangeRequestItemResponse));
    }

    private ShopImageChangeRequestItemResponse toShopImageChangeRequestItemResponse(ShopImageChangeRequestResult dto) {
        return ShopImageChangeRequestItemResponse.of(
            dto.id(),
            dto.shopId(),
            dto.imageType().name(),
            dto.imageUrl(),
            dto.status().name(),
            dto.rejectReason()
        );
    }
}
