package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 요청건 문의 스레드 JPA 영속 모델(append-only).
 *
 * <p>enum 필드에 {@code columnDefinition = "VARCHAR(20)"}을 병기하는 이유는
 * {@code ShopRequestIndexJpaEntity} Javadoc과 같다. {@code updated_at}은 수정 경로가 없어 항상
 * {@code created_at}과 같지만 {@code BaseEntity} 규약을 맞추기 위해 컬럼을 둔다.
 */
@Entity
@Table(name = "SHOP_REQUEST_COMMENT")
public class ShopRequestCommentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_request_index_id", nullable = false)
    private Long shopRequestIndexId; // SHOP_REQUEST_INDEX.id 참조

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopRequestCommentAuthorType authorType; // 작성자 유형 (CEO, ADMIN)

    @Column(name = "author_id", nullable = false)
    private Long authorId; // 작성자 ID (CEO.id 또는 ADMIN.id 참조)

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

    protected ShopRequestCommentJpaEntity() {
    }

    private ShopRequestCommentJpaEntity(
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content
    ) {
        this.shopRequestIndexId = shopRequestIndexId;
        this.authorType = authorType;
        this.authorId = authorId;
        this.content = content;
    }

    static ShopRequestCommentJpaEntity create(
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content
    ) {
        return new ShopRequestCommentJpaEntity(shopRequestIndexId, authorType, authorId, content);
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
}
