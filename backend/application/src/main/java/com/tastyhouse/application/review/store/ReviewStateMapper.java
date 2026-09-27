package com.tastyhouse.application.review.store;

import com.tastyhouse.application.review.port.out.write.ReviewState;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReviewStateMapper {
    private ReviewStateMapper() {
    }

    static Review toDomain(ReviewState state) {
        return Review.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.productId() == null ? null : ProductId.of(state.productId()),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.content(),
            state.totalRating(),
            state.tasteRating(),
            state.amountRating(),
            state.priceRating(),
            state.atmosphereRating(),
            state.kindnessRating(),
            state.hygieneRating(),
            state.willRevisit(),
            state.orderId() == null ? null : OrderId.of(state.orderId()),
            state.hidden(),
            state.ownerOnly(),
            state.deliveryRating(),
            state.deliveryComment(),
            state.createdAt()
        );
    }

    static ReviewState toState(Review review) {
        return new ReviewState(
            review.getId(),
            review.getShopId() == null ? null : review.getShopId().value(),
            review.getProductId() == null ? null : review.getProductId().value(),
            review.getMemberId() == null ? null : review.getMemberId().value(),
            review.getContent(),
            review.getTotalRating(),
            review.getTasteRating(),
            review.getAmountRating(),
            review.getPriceRating(),
            review.getAtmosphereRating(),
            review.getKindnessRating(),
            review.getHygieneRating(),
            review.isWillRevisit(),
            review.getOrderId() == null ? null : review.getOrderId().value(),
            review.isHidden(),
            review.isOwnerOnly(),
            review.getDeliveryRating(),
            review.getDeliveryComment(),
            review.getCreatedAt()
        );
    }
}
