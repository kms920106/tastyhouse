package com.tastyhouse.core.entity.review;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "REVIEW_LIKE")
public class ReviewLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "review_id", nullable = false)
    private Long reviewId; // 리뷰 ID (REVIEW.id 참조)

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 좋아요를 누른 회원 ID (MEMBER.id 참조)

    protected ReviewLike() {
    }

    public ReviewLike(Long reviewId, Long memberId) {
        this.reviewId = reviewId;
        this.memberId = memberId;
    }
}
