package com.tastyhouse.adminapi.shop;

import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopImageChangeRequestResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.file.FileService;
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
    private final FileService fileService;

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

        // 목록 항목마다 이미지 URL을 단건 조회하면 항목 수만큼 쿼리가 나가므로(N+1), 파일 식별자를 모아
        // 한 번에 변환한 뒤 매핑한다.
        Map<Long, String> imageUrls = fileService.getUrlsByFileIds(
            pageResult.content().stream()
                .map(ShopImageChangeRequestResult::imageFileId)
                .filter(Objects::nonNull)
                .toList()
        );

        return PaginationResponse.from(pageResult.map(dto -> toShopImageChangeRequestItemResponse(dto, imageUrls)));
    }

    private ShopImageChangeRequestItemResponse toShopImageChangeRequestItemResponse(
        ShopImageChangeRequestResult dto,
        Map<Long, String> imageUrls
    ) {
        return ShopImageChangeRequestItemResponse.of(
            dto.id(),
            dto.shopId(),
            dto.imageType().name(),
            dto.imageFileId() == null ? null : imageUrls.get(dto.imageFileId()),
            dto.status().name(),
            dto.rejectReason()
        );
    }
}
