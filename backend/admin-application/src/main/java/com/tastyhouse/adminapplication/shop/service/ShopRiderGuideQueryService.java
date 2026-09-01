package com.tastyhouse.adminapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import com.tastyhouse.adminapplication.shop.port.in.ShopRiderGuideQueryUseCase;

/**
 * admin용 라이더 안내 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 가게의 라이더 안내를 조회한다(admin 무제한 원칙).
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopRiderGuideQueryService implements ShopRiderGuideQueryUseCase {

    private final ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort;

    public ShopRiderGuideQueryService(ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort) {
        this.shopRiderGuideManagementQueryPort = shopRiderGuideManagementQueryPort;
    }

    @Override
    public PageResult<ShopRiderGuideListItemResult> getRiderGuides(
        String shopName,
        Boolean hasVisitGuide,
        int page,
        int size
    ) {
        return shopRiderGuideManagementQueryPort.findRiderGuidePage(shopName, hasVisitGuide, PageQuery.of(page, size));
    }

    @Override
    public ShopRiderGuideDetail getRiderGuide(Long shopId) {
        ShopRiderGuideResult result = shopRiderGuideManagementQueryPort.findRiderGuide(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        return new ShopRiderGuideDetail(result, shopRiderGuideManagementQueryPort.findHistories(shopId));
    }
}
