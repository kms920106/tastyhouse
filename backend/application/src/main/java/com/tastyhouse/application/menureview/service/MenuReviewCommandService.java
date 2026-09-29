package com.tastyhouse.application.menureview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
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
import com.tastyhouse.application.shared.marker.WebApp;

@Service
@WebApp
@Transactional
public class MenuReviewCommandService implements MenuReviewCommandUseCase {

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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Order order = orderPersistencePort.findById(orderProduct.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        MemberId targetMemberId = MemberId.of(command.memberId());
        if (!order.getMemberId().equals(targetMemberId)) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_ACCESS_DENIED);
        }

        Product product = productPersistencePort.findByIdIncludingDeleted(orderProduct.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isRatingExcluded()) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_NOT_ALLOWED);
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
