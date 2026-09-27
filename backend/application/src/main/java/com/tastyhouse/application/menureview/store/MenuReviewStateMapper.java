package com.tastyhouse.application.menureview.store;

import com.tastyhouse.application.menureview.port.out.write.MenuReviewState;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class MenuReviewStateMapper {
    private MenuReviewStateMapper() {
    }

    static MenuReview toDomain(MenuReviewState state) {
        return MenuReview.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.orderId() == null ? null : OrderId.of(state.orderId()),
            state.orderProductId() == null ? null : OrderProductId.of(state.orderProductId()),
            state.rating(),
            state.comment(),
            state.hidden(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static MenuReviewState toState(MenuReview menuReview) {
        return new MenuReviewState(
            menuReview.getId(),
            menuReview.getMemberId() == null ? null : menuReview.getMemberId().value(),
            menuReview.getShopId() == null ? null : menuReview.getShopId().value(),
            menuReview.getProductId() == null ? null : menuReview.getProductId().value(),
            menuReview.getOrderId() == null ? null : menuReview.getOrderId().value(),
            menuReview.getOrderProductId() == null ? null : menuReview.getOrderProductId().value(),
            menuReview.getRating(),
            menuReview.getComment(),
            menuReview.isHidden(),
            menuReview.getCreatedAt(),
            menuReview.getUpdatedAt()
        );
    }
}
