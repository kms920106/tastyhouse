package com.tastyhouse.application.menureview.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.service.MenuReviewLifecycleService;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.menureview.port.in.MenuReviewCommandUseCase;
import com.tastyhouse.application.menureview.port.in.MenuReviewCreateCommand;
import com.tastyhouse.application.menureview.port.in.MenuReviewDeleteCommand;
import com.tastyhouse.application.menureview.port.in.MenuReviewUpdateCommand;

@Service
@WebApp
@Transactional
public class MenuReviewCommandService implements MenuReviewCommandUseCase {

    private final MenuReviewLifecycleService menuReviewLifecycleService;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public MenuReviewCommandService(
        MenuReviewLifecycleService menuReviewLifecycleService,
        OrderProductRepository orderProductRepository,
        OrderRepository orderRepository,
        ProductRepository productRepository
    ) {
        this.menuReviewLifecycleService = menuReviewLifecycleService;
        this.orderProductRepository = orderProductRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Long createMenuReview(MenuReviewCreateCommand command) {
        OrderProduct orderProduct = orderProductRepository.findById(OrderProductId.of(command.orderProductId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Order order = orderRepository.findById(orderProduct.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        MemberId targetMemberId = MemberId.of(command.memberId());
        if (!order.getMemberId().equals(targetMemberId)) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_ACCESS_DENIED);
        }

        Product product = productRepository.findByIdIncludingDeleted(orderProduct.getProductId())
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
