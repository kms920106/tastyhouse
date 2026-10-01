package com.tastyhouse.infrastructure.menureview.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class MenuReviewMapper {

    private MenuReviewMapper() {
    }

    static MenuReview toDomain(MenuReviewJpaEntity entity) {
        return MenuReview.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getOrderId() == null ? null : OrderId.of(entity.getOrderId()),
            entity.getOrderProductId() == null ? null : OrderProductId.of(entity.getOrderProductId()),
            entity.getRating(),
            entity.getComment(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MenuReviewJpaEntity toEntity(MenuReview menuReview) {
        return MenuReviewJpaEntity.create(
            menuReview.getMemberId() == null ? null : menuReview.getMemberId().value(),
            menuReview.getShopId() == null ? null : menuReview.getShopId().value(),
            menuReview.getProductId() == null ? null : menuReview.getProductId().value(),
            menuReview.getOrderId() == null ? null : menuReview.getOrderId().value(),
            menuReview.getOrderProductId() == null ? null : menuReview.getOrderProductId().value(),
            menuReview.getRating(),
            menuReview.getComment(),
            menuReview.isHidden()
        );
    }

    static void applyChanges(MenuReviewJpaEntity entity, MenuReview menuReview) {
        entity.applyChanges(
            menuReview.getRating(),
            menuReview.getComment(),
            menuReview.isHidden()
        );
    }
}
