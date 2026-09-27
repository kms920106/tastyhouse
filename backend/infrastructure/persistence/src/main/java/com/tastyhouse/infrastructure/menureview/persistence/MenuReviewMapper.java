package com.tastyhouse.infrastructure.menureview.persistence;

import com.tastyhouse.application.menureview.port.out.write.MenuReviewState;

final class MenuReviewMapper {
    private MenuReviewMapper() {
    }

    static MenuReviewState toState(MenuReviewJpaEntity entity) {
        return new MenuReviewState(
            entity.getId(),
            entity.getMemberId(),
            entity.getShopId(),
            entity.getProductId(),
            entity.getOrderId(),
            entity.getOrderProductId(),
            entity.getRating(),
            entity.getComment(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MenuReviewJpaEntity toEntity(MenuReviewState state) {
        return MenuReviewJpaEntity.create(
            state.memberId(),
            state.shopId(),
            state.productId(),
            state.orderId(),
            state.orderProductId(),
            state.rating(),
            state.comment(),
            state.hidden()
        );
    }

    static void applyChanges(MenuReviewJpaEntity entity, MenuReviewState state) {
        entity.applyChanges(
            state.rating(),
            state.comment(),
            state.hidden()
        );
    }
}
