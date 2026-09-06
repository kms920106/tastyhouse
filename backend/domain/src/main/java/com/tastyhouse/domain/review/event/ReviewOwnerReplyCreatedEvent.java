package com.tastyhouse.domain.review.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.domain.shop.vo.ShopId;

public record ReviewOwnerReplyCreatedEvent(
    ReviewId reviewId,
    MemberId reviewerMemberId,
    ShopId shopId,
    ReviewOwnerReplyId ownerReplyId,
    LocalDateTime occurredAt
) {
}
