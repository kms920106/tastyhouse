package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 점주 공지 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopNotice}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopNoticeMapper}가 수행한다.
 */
@Entity
@Table(
    name = "SHOP_NOTICE",
    indexes = {
        @Index(name = "idx_shop_notice_shop_id", columnList = "shop_id"),
        @Index(name = "idx_shop_notice_exposed", columnList = "shop_id, is_exposed, is_hidden")
    }
)
public class ShopNoticeJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_exposed", nullable = false)
    private boolean exposed;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    protected ShopNoticeJpaEntity() {
    }

    private ShopNoticeJpaEntity(Long shopId, String content, boolean exposed, boolean hidden) {
        this.shopId = shopId;
        this.content = content;
        this.exposed = exposed;
        this.hidden = hidden;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopNoticeMapper#toEntity}에서만 호출한다.
     */
    static ShopNoticeJpaEntity create(Long shopId, String content, boolean exposed, boolean hidden) {
        return new ShopNoticeJpaEntity(shopId, content, exposed, hidden);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체).
     * 감사 필드·식별자·shopId는 건드리지 않는다.
     */
    void applyChanges(String content, boolean exposed, boolean hidden) {
        this.content = content;
        this.exposed = exposed;
        this.hidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isExposed() {
        return this.exposed;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
