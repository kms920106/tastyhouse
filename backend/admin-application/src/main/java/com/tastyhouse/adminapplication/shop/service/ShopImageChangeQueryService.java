package com.tastyhouse.adminapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.adminapplication.shop.port.in.ShopImageChangeQueryUseCase;

/**
 * admin용 가게 이미지(상표/대표이미지) 변경요청 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 요청을 승인 상태·이미지 유형으로 필터해 조회한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopImageChangeQueryService implements ShopImageChangeQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopImageChangeQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public PageResult<ShopImageChangeRequestResult> getImageChangeRequests(
        String status,
        String imageType,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = status == null ? null : ApprovalStatus.valueOf(status);
        ShopImageType type = imageType == null ? null : ShopImageType.from(imageType);

        return shopManagementQueryPort.findImageChangeRequestPage(approvalStatus, type, PageQuery.of(page, size));
    }
}
