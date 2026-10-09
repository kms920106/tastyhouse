package com.tastyhouse.application.menureview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.application.menureview.port.in.MenuReviewCreateCommand;
import com.tastyhouse.application.menureview.port.in.MenuReviewCreateUseCase;
import com.tastyhouse.application.order.port.out.write.OrderLoadPort;
import com.tastyhouse.application.order.port.out.write.OrderProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional
class MenuReviewCreateService implements MenuReviewCreateUseCase {

    private final MenuReviewLifecycleService menuReviewLifecycleService;
    private final OrderProductLoadPort orderProductLoadPort;
    private final OrderLoadPort orderLoadPort;
    private final ProductLoadPort productLoadPort;

    public MenuReviewCreateService(
        MenuReviewLifecycleService menuReviewLifecycleService,
        OrderProductLoadPort orderProductLoadPort,
        OrderLoadPort orderLoadPort,
        ProductLoadPort productLoadPort
    ) {
        this.menuReviewLifecycleService = menuReviewLifecycleService;
        this.orderProductLoadPort = orderProductLoadPort;
        this.orderLoadPort = orderLoadPort;
        this.productLoadPort = productLoadPort;
    }

    @Override
    public Long createMenuReview(MenuReviewCreateCommand command) {
        OrderProduct orderProduct = orderProductLoadPort.findById(OrderProductId.of(command.orderProductId()))
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Order order = orderLoadPort.findByIdIncludingDeleted(orderProduct.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
        MemberId targetMemberId = MemberId.of(command.memberId());
        if (!order.getMemberId().equals(targetMemberId)) {
            throw new ApplicationException(WebErrorCode.MENU_REVIEW_ACCESS_DENIED);
        }

        Product product = productLoadPort.findByIdIncludingDeleted(orderProduct.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (product.isRatingExcluded()) {
            throw new DomainException(DomainErrorCode.MENU_REVIEW_NOT_ALLOWED);
        }

        return menuReviewLifecycleService.register(
            targetMemberId,
            order.getShopId(),
            orderProduct.getProductId(),
            orderProduct.getOrderId(),
            orderProduct.getOrderProductId(),
            command.rating(),
            command.comment()
        );
    }
}
