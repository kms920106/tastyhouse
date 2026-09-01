package com.tastyhouse.ceoapplication.shop.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeCatalogResult;

/**
 * 가게 요청 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopRequestQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopRequestQueryUseCase {

    PageResult<ShopRequestListItemViewResult> getRequests(
        Long ceoId,
        Long shopId,
        String requestType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );

    ShopRequestDetailViewResult getRequestDetail(Long ceoId, Long shopId, Long requestId);

    List<ShopRequestCommentResult> getComments(Long ceoId, Long shopId, Long requestId);

    ShopRequestTypeCatalogResult getRequestTypes();
}
