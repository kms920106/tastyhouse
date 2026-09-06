package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_CHOICE")
public class ShopChoiceJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    protected ShopChoiceJpaEntity() {
    }

    private ShopChoiceJpaEntity(Long shopId, String title, String content) {
        this.shopId = shopId;
        this.title = title;
        this.content = content;
    }

    static ShopChoiceJpaEntity create(Long shopId, String title, String content) {
        return new ShopChoiceJpaEntity(shopId, title, content);
    }

    void applyChanges(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }
}
