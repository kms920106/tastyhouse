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

@Entity
@Table(name = "SHOP_ORIGIN_INFO")
public class ShopOriginInfoJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OriginSourceType sourceType;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "url", length = 500)
    private String url;

    protected ShopOriginInfoJpaEntity() {
    }

    private ShopOriginInfoJpaEntity(Long shopId, OriginSourceType sourceType, String content, String url) {
        this.shopId = shopId;
        this.sourceType = sourceType;
        this.content = content;
        this.url = url;
    }

    static ShopOriginInfoJpaEntity create(Long shopId, OriginSourceType sourceType, String content, String url) {
        return new ShopOriginInfoJpaEntity(shopId, sourceType, content, url);
    }

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
