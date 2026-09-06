package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 사장님 답변 순수 도메인 모델 — 리뷰 1건당 1개.
 *
 * <p>기존 {@code ReviewComment}는 회원이 다는 댓글이고 {@code member_id}만 있어 점주를 표현할 수 없다.
 * 회원 댓글/답글 구조를 건드리지 않고 별도 애그리거트로 둔 덕분에 web/admin의 기존 댓글 조회·삭제 경로에
 * 회귀가 없다.
 *
 * <p><b>리뷰당 1건 제약은 {@code UNIQUE(review_id)}가 물리적으로 보증한다</b> — 애플리케이션의
 * {@code existsByReviewId} 검사는 사용자에게 409를 돌려주기 위한 것이고, 동시 요청의 최종 방어선은 그
 * 유니크 제약이다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReviewOwnerReplyJpaEntity} + {@code ReviewOwnerReplyMapper}가 담당하며, 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 명시적 {@code save} 호출이다.
 */
public class ReviewOwnerReply {

    /**
     * 답변 <b>작성</b> 가능 기간(일) — 리뷰 작성일로부터 이 일수까지만 신규 등록할 수 있다.
     *
     * <p>원문: "사장님 댓글은 고객의 리뷰 작성 완료일로부터 30일 이내 작성할 수 있어요."
     *
     * <p><b>등록에만 적용된다.</b> 이미 단 답변의 수정·삭제는 기간이 지나도 막지 않는다 — 원문이
     * 제한하는 것은 "작성"이고, 30일 뒤 오타 수정조차 막는 것은 과하기 때문이다.
     */
    public static final int REPLY_PERIOD_DAYS = 30;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ReviewId reviewId;
    private final ShopId shopId;
    private final CeoId ceoId;
    private String content;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ReviewOwnerReply(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 답변을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ReviewOwnerReply of(ReviewId reviewId, ShopId shopId, CeoId ceoId, String content) {
        return new ReviewOwnerReply(null, reviewId, shopId, ceoId, content, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ReviewOwnerReply reconstitute(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ReviewOwnerReply(id, reviewId, shopId, ceoId, content, createdAt, updatedAt);
    }

    /**
     * 답변 내용을 수정한다. 작성자(점주)는 바뀌지 않으므로 {@code ceoId}는 재대입하지 않는다 — 소유권
     * 검증은 가게 기준이고, 같은 가게의 다른 점주 계정이 수정하더라도 최초 작성자를 이력으로 남긴다.
     */
    public void updateContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
