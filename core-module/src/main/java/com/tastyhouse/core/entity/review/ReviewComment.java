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
@Table(name = "REVIEW_COMMENT")
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "review_id", nullable = false)
    private Long reviewId; // 리뷰 ID (REVIEW.id 참조)

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 댓글 작성자 회원 ID (MEMBER.id 참조)

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // 댓글 내용

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false; // 숨김 여부 (true: 관리자에 의해 숨김 처리)

    protected ReviewComment() {
    }

    public ReviewComment(Long reviewId, Long memberId, String content) {
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.content = content;
    }
}
