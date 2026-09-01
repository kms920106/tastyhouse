package com.tastyhouse.adminapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.adminapplication.shop.port.in.ShopContentBoardQueryUseCase;

/**
 * admin용 가게 콘텐츠보드 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 가게 콘텐츠보드를 가게·숨김여부·콘텐츠 유형으로 필터해 조회한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ShopContentBoardQueryService implements ShopContentBoardQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopContentBoardQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public PageResult<ShopContentBoardResult> getContentBoards(
        Long shopId,
        Boolean hidden,
        String contentType,
        int page,
        int size
    ) {
        ShopContentType type = contentType == null ? null : ShopContentType.from(contentType);

        return shopManagementQueryPort.findContentBoardPage(shopId, hidden, type, PageQuery.of(page, size));
    }
}
