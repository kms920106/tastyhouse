package com.tastyhouse.adminapi.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.domain.shop.application.ShopImageChangeCommandService;
import com.tastyhouse.core.domain.shop.application.ShopImageChangeQueryService;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopImageChangeRequestResult;
import com.tastyhouse.core.shared.model.ApprovalStatus;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.response.ShopImageChangeRequestItemResponse;

/**
 * admin용 가게 이미지(상표/대표이미지) 변경요청 검수 중개 서비스. 소유권 검증 없이 전체 요청을
 * 조회·승인·반려한다.
 */
@Service
@RequiredArgsConstructor
public class ShopImageChangeAdminService {

    private final ShopImageChangeCommandService shopImageChangeCommandService;
    private final ShopImageChangeQueryService shopImageChangeQueryService;

    public PaginationResponse<ShopImageChangeRequestItemResponse> getImageChangeRequests(String status, String imageType, int page, int size) {
        ApprovalStatus approvalStatus = status == null ? null : ApprovalStatus.valueOf(status);
        ShopImageType type = imageType == null ? null : ShopImageType.from(imageType);

        PageResult<ShopImageChangeRequestItemResponse> pageResult = shopImageChangeQueryService
            .findRequests(approvalStatus, type, page, size)
            .map(this::toShopImageChangeRequestItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public void approveImageChange(Long id) {
        shopImageChangeCommandService.approveImageChange(id);
    }

    public void rejectImageChange(Long id, String reason) {
        shopImageChangeCommandService.rejectImageChange(id, reason);
    }

    private ShopImageChangeRequestItemResponse toShopImageChangeRequestItemResponse(ShopImageChangeRequestResult dto) {
        return ShopImageChangeRequestItemResponse.of(
            dto.id(),
            dto.shopId(),
            dto.imageType().name(),
            dto.imageFileId(),
            dto.status().name(),
            dto.rejectReason()
        );
    }
}
