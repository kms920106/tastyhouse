package com.tastyhouse.domain.review.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

/**
 * 리뷰 댓글 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReviewCommentJpaEntity} + {@code ReviewCommentMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ReviewCommentRepository#save}를
 * 호출해야 한다.
 */
public class ReviewComment {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ReviewId reviewId;
    private final MemberId memberId;
    private final String content;
    private boolean hidden;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ReviewComment(
        Long id,
        ReviewId reviewId,
        MemberId memberId,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.content = content;
        this.hidden = hidden;
        this.createdAt = createdAt;
    }

    /**
     * 신규 리뷰 댓글을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ReviewComment of(ReviewId reviewId, MemberId memberId, String content) {
        return new ReviewComment(null, reviewId, memberId, content, false, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ReviewComment reconstitute(
        Long id,
        ReviewId reviewId,
        MemberId memberId,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new ReviewComment(id, reviewId, memberId, content, hidden, createdAt);
    }

    public ReviewCommentId getReviewCommentId() {
        return ReviewCommentId.of(this.id);
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
