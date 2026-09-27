package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_ORIGIN_INFO")
public class ShopOriginInfoJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "source_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String sourceType;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "url", length = 500)
    private String url;

    protected ShopOriginInfoJpaEntity() {
    }

    private ShopOriginInfoJpaEntity(Long shopId, String sourceType, String content, String url) {
        this.shopId = shopId;
        this.sourceType = sourceType;
        this.content = content;
        this.url = url;
    }

    static ShopOriginInfoJpaEntity create(Long shopId, String sourceType, String content, String url) {
        return new ShopOriginInfoJpaEntity(shopId, sourceType, content, url);
    }

    void applyChanges(String sourceType, String content, String url) {
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

    public String getSourceType() {
        return this.sourceType;
    }

    public String getContent() {
        return this.content;
    }

    public String getUrl() {
        return this.url;
    }
}
