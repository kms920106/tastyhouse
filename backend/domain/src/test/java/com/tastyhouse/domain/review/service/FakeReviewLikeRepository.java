package com.tastyhouse.domain.review.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.repository.ReviewLikeRepository;
import com.tastyhouse.domain.review.vo.ReviewId;

public class FakeReviewLikeRepository implements ReviewLikeRepository {
    private final List<ReviewLike> likes = new ArrayList<>();

    @Override
    public boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        return likes.stream().anyMatch(like ->
            like.getReviewId().equals(reviewId) && like.getMemberId().equals(memberId));
    }

    @Override
    public void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        likes.removeIf(like -> like.getReviewId().equals(reviewId) && like.getMemberId().equals(memberId));
    }

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        likes.add(reviewLike);
        return reviewLike;
    }
}
