package com.tastyhouse.infrastructure.jpa.review.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;

import static com.tastyhouse.infrastructure.jpa.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;

final class ReviewQueryPredicates {

    private ReviewQueryPredicates() {
    }

    static BooleanExpression visibleToCustomer() {
        return reviewJpaEntity.hidden.isFalse().and(reviewJpaEntity.ownerOnly.isFalse());
    }

    static NumberPath<Long> shopStationId() {
        return Expressions.numberPath(Long.class, shopJpaEntity, "stationId");
    }

    static NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
    }
}
