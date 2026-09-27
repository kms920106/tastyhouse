package com.tastyhouse.application.review.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.TagId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewStateMapperTest {

    @Test
    @DisplayName("Review → ReviewState → Review 왕복 시 모든 필드가 보존된다")
    void reviewRoundTrip() {
        Review original = Review.reconstitute(
            101L, ShopId.of(102L), ProductId.of(103L), MemberId.of(104L), "리뷰 본문",
            4.5, 4.1, 3.2, 2.3, 1.4, 3.5, 2.6, true, OrderId.of(105L), false, true,
            3, "배달 코멘트", LocalDateTime.of(2026, 7, 1, 9, 0));

        Review restored = ReviewStateMapper.toDomain(ReviewStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewImage → ReviewImageState → ReviewImage 왕복 시 모든 필드가 보존된다")
    void reviewImageRoundTrip() {
        ReviewImage original = ReviewImage.reconstitute(111L, ReviewId.of(112L), UploadedFileId.of(113L), 2);

        ReviewImage restored = ReviewImageStateMapper.toDomain(ReviewImageStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewLike → ReviewLikeState → ReviewLike 왕복 시 모든 필드가 보존된다")
    void reviewLikeRoundTrip() {
        ReviewLike original = ReviewLike.reconstitute(121L, ReviewId.of(122L), MemberId.of(123L));

        ReviewLike restored = ReviewLikeStateMapper.toDomain(ReviewLikeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewComment → ReviewCommentState → ReviewComment 왕복 시 모든 필드가 보존된다")
    void reviewCommentRoundTrip() {
        ReviewComment original = ReviewComment.reconstitute(
            131L, ReviewId.of(132L), MemberId.of(133L), "댓글 내용", true, LocalDateTime.of(2026, 7, 2, 10, 0));

        ReviewComment restored = ReviewCommentStateMapper.toDomain(ReviewCommentStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewReply → ReviewReplyState → ReviewReply 왕복 시 모든 필드가 보존된다")
    void reviewReplyRoundTrip() {
        ReviewReply original = ReviewReply.reconstitute(
            141L, ReviewCommentId.of(142L), MemberId.of(143L), MemberId.of(144L), "답글 내용", true,
            LocalDateTime.of(2026, 7, 3, 11, 0));

        ReviewReply restored = ReviewReplyStateMapper.toDomain(ReviewReplyStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewOwnerReply → ReviewOwnerReplyState → ReviewOwnerReply 왕복 시 모든 필드가 보존된다")
    void reviewOwnerReplyRoundTrip() {
        ReviewOwnerReply original = ReviewOwnerReply.reconstitute(
            151L, ReviewId.of(152L), ShopId.of(153L), CeoId.of(154L), "사장님 답변",
            LocalDateTime.of(2026, 7, 4, 12, 0), LocalDateTime.of(2026, 7, 5, 13, 0));

        ReviewOwnerReply restored = ReviewOwnerReplyStateMapper.toDomain(ReviewOwnerReplyStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewTag → ReviewTagState → ReviewTag 왕복 시 모든 필드가 보존된다")
    void reviewTagRoundTrip() {
        ReviewTag original = ReviewTag.reconstitute(161L, ReviewId.of(162L), TagId.of(163L));

        ReviewTag restored = ReviewTagStateMapper.toDomain(ReviewTagStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewBlindRequest → ReviewBlindRequestState → ReviewBlindRequest 왕복 시 모든 필드가 보존된다")
    void reviewBlindRequestRoundTrip() {
        ReviewBlindRequest original = ReviewBlindRequest.reconstitute(
            171L, ReviewId.of(172L), ShopId.of(173L), CeoId.of(174L), ReviewBlindReason.PRIVACY, "상세 사유",
            ReviewBlindStatus.APPROVED, "반려 사유", LocalDateTime.of(2026, 7, 6, 14, 0),
            LocalDateTime.of(2026, 7, 7, 15, 0));

        ReviewBlindRequest restored =
            ReviewBlindRequestStateMapper.toDomain(ReviewBlindRequestStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReviewBlindRequestAttachment → State → ReviewBlindRequestAttachment 왕복 시 모든 필드가 보존된다")
    void reviewBlindRequestAttachmentRoundTrip() {
        ReviewBlindRequestAttachment original = ReviewBlindRequestAttachment.reconstitute(
            181L, ReviewBlindRequestId.of(182L), UploadedFileId.of(183L), 4);

        ReviewBlindRequestAttachment restored = ReviewBlindRequestAttachmentStateMapper.toDomain(
            ReviewBlindRequestAttachmentStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopReviewDisplaySetting → State → ShopReviewDisplaySetting 왕복 시 모든 필드가 보존된다")
    void shopReviewDisplaySettingRoundTrip() {
        ShopReviewDisplaySetting original = ShopReviewDisplaySetting.reconstitute(
            191L, ShopId.of(192L), ReviewSortType.OLDEST, LocalDateTime.of(2026, 7, 8, 16, 0));

        ShopReviewDisplaySetting restored =
            ShopReviewDisplaySettingStateMapper.toDomain(ShopReviewDisplaySettingStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
