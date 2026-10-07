package com.tastyhouse.application.menureview.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.application.menureview.port.in.MenuReviewWritableItemQueryUseCase;
import com.tastyhouse.application.menureview.port.out.MenuReviewQueryPort;
import com.tastyhouse.application.menureview.port.out.MenuReviewWritableItemResult;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class MenuReviewWritableItemQueryService implements MenuReviewWritableItemQueryUseCase {

    private final MenuReviewQueryPort menuReviewQueryPort;
    private final OrderQueryPort orderQueryPort;

    public MenuReviewWritableItemQueryService(MenuReviewQueryPort menuReviewQueryPort, OrderQueryPort orderQueryPort) {
        this.menuReviewQueryPort = menuReviewQueryPort;
        this.orderQueryPort = orderQueryPort;
    }

    @Override
    public List<MenuReviewWritableItemResult> findWritableItems(Long orderId, Long memberId) {
        Long orderMemberId = orderQueryPort.findOrderMemberId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
        if (!orderMemberId.equals(memberId)) {
            throw new DomainException(DomainErrorCode.ORDER_ACCESS_DENIED);
        }

        return menuReviewQueryPort.findWritableItemsByOrderId(orderId);
    }
}
