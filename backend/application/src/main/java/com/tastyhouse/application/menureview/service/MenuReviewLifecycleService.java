package com.tastyhouse.application.menureview.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.event.MenuReviewCreatedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewDeletedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewRatingChangedEvent;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.menureview.port.out.write.MenuReviewPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public class MenuReviewLifecycleService {

    private final MenuReviewPersistencePort menuReviewPersistencePort;
    private final DomainEventPublisher domainEventPublisher;

    public MenuReviewLifecycleService(
        MenuReviewPersistencePort menuReviewPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.menuReviewPersistencePort = menuReviewPersistencePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Long register(
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment
    ) {
        if (menuReviewPersistencePort.existsByOrderProductId(orderProductId)) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_ALREADY_EXISTS);
        }

        MenuReview saved = menuReviewPersistencePort.save(
            MenuReview.of(memberId, shopId, productId, orderId, orderProductId, rating, comment)
        );

        domainEventPublisher.publish(new MenuReviewCreatedEvent(
            saved.getMenuReviewId(),
            memberId,
            shopId,
            productId,
            LocalDateTime.now()
        ));

        return saved.getId();
    }

    public void modify(MenuReviewId menuReviewId, MemberId memberId, Integer rating, String comment) {
        MenuReview menuReview = loadOwnedBy(menuReviewId, memberId);

        menuReview.updateRating(rating, comment);
        menuReviewPersistencePort.save(menuReview);

        domainEventPublisher.publish(new MenuReviewRatingChangedEvent(
            menuReviewId,
            memberId,
            menuReview.getShopId(),
            menuReview.getProductId(),
            LocalDateTime.now()
        ));
    }

    public void remove(MenuReviewId menuReviewId, MemberId memberId) {
        MenuReview menuReview = loadOwnedBy(menuReviewId, memberId);

        menuReviewPersistencePort.deleteById(menuReviewId);

        domainEventPublisher.publish(new MenuReviewDeletedEvent(
            menuReviewId,
            memberId,
            menuReview.getShopId(),
            menuReview.getProductId(),
            LocalDateTime.now()
        ));
    }

    private MenuReview loadOwnedBy(MenuReviewId menuReviewId, MemberId memberId) {
        return menuReviewPersistencePort.findByIdAndMemberId(menuReviewId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_REVIEW_ACCESS_DENIED));
    }
}
