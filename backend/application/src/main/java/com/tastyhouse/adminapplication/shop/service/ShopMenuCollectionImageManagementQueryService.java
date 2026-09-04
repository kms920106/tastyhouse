package com.tastyhouse.adminapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.adminapplication.shop.port.in.ShopMenuCollectionImageManagementQueryUseCase;

/**
 * 메뉴모음컷 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 요청을 승인 상태로 필터해 조회한다 — 관리자는 모든 가게의 요청을 본다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopMenuCollectionImageManagementQueryService implements ShopMenuCollectionImageManagementQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopMenuCollectionImageManagementQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public PageResult<ShopMenuCollectionImageRequestResult> getMenuCollectionImageRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        return shopManagementQueryPort.findMenuCollectionImageRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    /** 상태 미지정({@code null})은 "전체"를 뜻하므로 승격하지 않는다. */
    private ApprovalStatus promoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status);
    }
}
