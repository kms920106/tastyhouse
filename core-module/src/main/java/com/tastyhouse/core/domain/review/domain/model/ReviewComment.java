package com.tastyhouse.core.domain.review.domain.model;

import com.tastyhouse.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "REVIEW_COMMENT")
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    protected ReviewComment() {
    }

    public ReviewComment(Long reviewId, Long memberId, String content) {
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.content = content;
    }
}
