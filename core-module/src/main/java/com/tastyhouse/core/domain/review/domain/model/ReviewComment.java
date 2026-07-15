package com.tastyhouse.core.domain.review.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.member.infrastructure.persistence.converter.MemberIdConverter;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(name = "REVIEW_COMMENT")
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_hidden", nullable = false)
    private final boolean hidden = false;

    protected ReviewComment() {
    }

    public ReviewComment(Long reviewId, MemberId memberId, String content) {
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.content = content;
    }

    public ReviewCommentId getReviewCommentId() {
        return ReviewCommentId.of(this.id);
    }
}
