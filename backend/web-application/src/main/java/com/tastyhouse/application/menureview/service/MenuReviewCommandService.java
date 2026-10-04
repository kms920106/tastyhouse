package com.tastyhouse.application.menureview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.application.menureview.port.in.MenuReviewCommandUseCase;
import com.tastyhouse.application.menureview.port.in.MenuReviewCreateCommand;
import com.tastyhouse.application.menureview.port.in.MenuReviewDeleteCommand;
import com.tastyhouse.application.menureview.port.in.MenuReviewUpdateCommand;
import com.tastyhouse.application.order.port.out.write.OrderPersistencePort;
import com.tastyhouse.application.order.port.out.write.OrderProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional
class MenuReviewCommandService implements MenuReviewCommandUseCase {

    private final MenuReviewLifecycleService menuReviewLifecycleService;
    private final OrderProductPersistencePort orderProductPersistencePort;
    private final OrderPersistencePort orderPersistencePort;
    private final ProductPersistencePort productPersistencePort;

    public MenuReviewCommandService(
        MenuReviewLifecycleService menuReviewLifecycleService,
        OrderProductPersistencePort orderProductPersistencePort,
        OrderPersistencePort orderPersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        this.menuReviewLifecycleService = menuReviewLifecycleService;
        this.orderProductPersistencePort = orderProductPersistencePort;
        this.orderPersistencePort = orderPersistencePort;
        this.productPersistencePort = productPersistencePort;
    }

    @Override
    public Long createMenuReview(MenuReviewCreateCommand command) {
        OrderProduct orderProduct = orderProductPersistencePort.findById(OrderProductId.of(command.orderProductId()))
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Order order = orderPersistencePort.findById(orderProduct.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
        MemberId targetMemberId = MemberId.of(command.memberId());
        if (!order.getMemberId().equals(targetMemberId)) {
            throw new ApplicationException(WebErrorCode.MENU_REVIEW_ACCESS_DENIED);
        }

        Product product = productPersistencePort.findByIdIncludingDeleted(orderProduct.getProductId())
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

    @Override
    public void updateMenuReview(MenuReviewUpdateCommand command) {
        MenuReviewId menuReviewId = MenuReviewId.of(command.menuReviewId());
        menuReviewLifecycleService.modify(menuReviewId, MemberId.of(command.memberId()), command.rating(), command.comment());
    }

    @Override
    public void deleteMenuReview(MenuReviewDeleteCommand command) {
        MenuReviewId menuReviewId = MenuReviewId.of(command.menuReviewId());
        menuReviewLifecycleService.remove(menuReviewId, MemberId.of(command.memberId()));
    }
}
