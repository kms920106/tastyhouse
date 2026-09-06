package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

/**
 * 요청건 문의 스레드의 댓글 1건(append-only).
 *
 * <p>스레드 단위는 {@link ShopRequestIndex} 행 1개다 — 요청 유형과 무관한 단일 테이블이 성립하는 근거는
 * 인덱스 행 id가 요청의 유일한 대외 식별자라는 점이다({@code REVIEW_COMMENT}는 Review FK 강결합이라
 * 재사용할 수 없다).
 *
 * <p>수정·삭제가 없어 전 필드가 final이고 상태전이 메서드도 없다. 요청이 반려·취소·승인된 뒤에도 작성할 수
 * 있다 — 반려 사유 문의가 이 기능의 주요 사용례이므로 상태 제약을 두지 않는다.
 */
public class ShopRequestComment {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long shopRequestIndexId;
    private final ShopRequestCommentAuthorType authorType;
    private final Long authorId; // CEO.id 또는 ADMIN.id
    private final String content;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재

    private ShopRequestComment(
        Long id,
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopRequestIndexId = shopRequestIndexId;
        this.authorType = authorType;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * 신규 댓글을 만든다. 작성자 유형·식별자는 도메인이 인증을 모르므로
     * {@link ShopRequestCommentAuthor}로 명시 전달받는다.
     */
    public static ShopRequestComment of(Long shopRequestIndexId, ShopRequestCommentAuthor author, String content) {
        return new ShopRequestComment(null, shopRequestIndexId, author.authorType(), author.authorId(), content, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopRequestComment reconstitute(
        Long id,
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content,
        LocalDateTime createdAt
    ) {
        return new ShopRequestComment(id, shopRequestIndexId, authorType, authorId, content, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopRequestIndexId() {
        return this.shopRequestIndexId;
    }

    public ShopRequestCommentAuthorType getAuthorType() {
        return this.authorType;
    }

    public Long getAuthorId() {
        return this.authorId;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
