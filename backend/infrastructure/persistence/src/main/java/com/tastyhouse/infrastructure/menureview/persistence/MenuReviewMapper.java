package com.tastyhouse.infrastructure.menureview.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MenuReviewMapper {
    private MenuReviewMapper() {
    }

    static MenuReview toDomain(MenuReviewJpaEntity entity) {
        return MenuReview.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getOrderId(), OrderId::of),
            IdMapping.vo(entity.getOrderProductId(), OrderProductId::of),
            entity.getRating(),
            entity.getComment(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MenuReviewJpaEntity toEntity(MenuReview domain) {
        return MenuReviewJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getOrderId(), OrderId::value),
            IdMapping.raw(domain.getOrderProductId(), OrderProductId::value),
            domain.getRating(),
            domain.getComment(),
            domain.isHidden()
        );
    }

    static void applyChanges(MenuReviewJpaEntity entity, MenuReview domain) {
        entity.applyChanges(
            domain.getRating(),
            domain.getComment(),
            domain.isHidden()
        );
    }
}
