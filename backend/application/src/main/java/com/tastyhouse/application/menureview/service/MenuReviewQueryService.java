package com.tastyhouse.application.menureview.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.menureview.port.out.MenuReviewListItemResult;
import com.tastyhouse.application.menureview.port.out.MenuReviewQueryPort;
import com.tastyhouse.application.menureview.port.out.MenuReviewWritableItemResult;
import com.tastyhouse.application.menureview.port.in.MenuReviewQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class MenuReviewQueryService implements MenuReviewQueryUseCase {

    private final MenuReviewQueryPort menuReviewQueryPort;
    private final OrderQueryPort orderQueryPort;

    public MenuReviewQueryService(MenuReviewQueryPort menuReviewQueryPort, OrderQueryPort orderQueryPort) {
        this.menuReviewQueryPort = menuReviewQueryPort;
        this.orderQueryPort = orderQueryPort;
    }

    @Override
    public List<MenuReviewWritableItemResult> findWritableItems(Long orderId, Long memberId) {
        Long orderMemberId = orderQueryPort.findOrderMemberId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        if (!orderMemberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return menuReviewQueryPort.findWritableItemsByOrderId(orderId);
    }

    @Override
    public PageResult<MenuReviewListItemResult> findByProductId(Long productId, int page, int size) {
        return menuReviewQueryPort.findVisibleByProductId(productId, PageQuery.of(page, size));
    }
}
