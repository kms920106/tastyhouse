package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 원산지 표시 정보 JPA 영속 모델. 순수 도메인 모델 {@code ShopOriginInfo}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_ORIGIN_INFO")
public class ShopOriginInfoJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OriginSourceType sourceType; // 입력 방식 (DIRECT, FRANCHISE_URL)

    @Column(name = "content", length = 2000)
    private String content; // 직접 입력 본문 (source_type=DIRECT)

    @Column(name = "url", length = 500)
    private String url; // 본사 제공 URL (source_type=FRANCHISE_URL)

    protected ShopOriginInfoJpaEntity() {
    }

    private ShopOriginInfoJpaEntity(Long shopId, OriginSourceType sourceType, String content, String url) {
        this.shopId = shopId;
        this.sourceType = sourceType;
        this.content = content;
        this.url = url;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopOriginInfoMapper#toEntity}에서만 호출한다.
     */
    static ShopOriginInfoJpaEntity create(Long shopId, OriginSourceType sourceType, String content, String url) {
        return new ShopOriginInfoJpaEntity(shopId, sourceType, content, url);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·shopId는 건드리지 않는다.
     */
    void applyChanges(OriginSourceType sourceType, String content, String url) {
        this.sourceType = sourceType;
        this.content = content;
        this.url = url;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public OriginSourceType getSourceType() {
        return this.sourceType;
    }

    public String getContent() {
        return this.content;
    }

    public String getUrl() {
        return this.url;
    }
}
