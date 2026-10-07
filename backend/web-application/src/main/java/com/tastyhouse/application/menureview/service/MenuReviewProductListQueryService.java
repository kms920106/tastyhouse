package com.tastyhouse.application.menureview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.menureview.port.in.MenuReviewProductListQueryUseCase;
import com.tastyhouse.application.menureview.port.out.MenuReviewListItemResult;
import com.tastyhouse.application.menureview.port.out.MenuReviewQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class MenuReviewProductListQueryService implements MenuReviewProductListQueryUseCase {

    private final MenuReviewQueryPort menuReviewQueryPort;

    public MenuReviewProductListQueryService(MenuReviewQueryPort menuReviewQueryPort) {
        this.menuReviewQueryPort = menuReviewQueryPort;
    }

    @Override
    public PageResult<MenuReviewListItemResult> findByProductId(Long productId, int page, int size) {
        return menuReviewQueryPort.findVisibleByProductId(productId, PageQuery.of(page, size));
    }
}
