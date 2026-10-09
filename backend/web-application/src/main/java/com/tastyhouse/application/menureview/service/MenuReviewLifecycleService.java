package com.tastyhouse.application.menureview.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

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
import com.tastyhouse.application.menureview.port.out.write.MenuReviewLoadPort;
import com.tastyhouse.application.menureview.port.out.write.MenuReviewSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class MenuReviewLifecycleService {

    private final MenuReviewLoadPort menuReviewLoadPort;
    private final MenuReviewSavePort menuReviewSavePort;
    private final DomainEventPublisher domainEventPublisher;

    public MenuReviewLifecycleService(
        MenuReviewLoadPort menuReviewLoadPort,
        MenuReviewSavePort menuReviewSavePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.menuReviewLoadPort = menuReviewLoadPort;
        this.menuReviewSavePort = menuReviewSavePort;
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
        if (menuReviewLoadPort.existsByOrderProductId(orderProductId)) {
            throw new ApplicationException(WebErrorCode.MENU_REVIEW_ALREADY_EXISTS);
        }

        MenuReview saved = menuReviewSavePort.save(
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
        menuReviewSavePort.save(menuReview);

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

        menuReviewSavePort.deleteById(menuReviewId);

        domainEventPublisher.publish(new MenuReviewDeletedEvent(
            menuReviewId,
            memberId,
            menuReview.getShopId(),
            menuReview.getProductId(),
            LocalDateTime.now()
        ));
    }

    private MenuReview loadOwnedBy(MenuReviewId menuReviewId, MemberId memberId) {
        return menuReviewLoadPort.findByIdAndMemberId(menuReviewId, memberId)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.MENU_REVIEW_ACCESS_DENIED));
    }
}
