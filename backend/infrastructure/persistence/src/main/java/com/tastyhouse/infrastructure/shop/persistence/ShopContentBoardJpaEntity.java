package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_CONTENT_BOARD")
public class ShopContentBoardJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "content_type", nullable = false, length = 10, columnDefinition = "VARCHAR(10)")
    private String contentType;

    @Column(name = "topic", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String topic;

    @Column(name = "image_file_id")
    private Long imageFileId;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    protected ShopContentBoardJpaEntity() {
    }

    private ShopContentBoardJpaEntity(
        Long shopId,
        String contentType,
        String topic,
        Long imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden
    ) {
        this.shopId = shopId;
        this.contentType = contentType;
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
        this.hidden = hidden;
    }

    static ShopContentBoardJpaEntity create(
        Long shopId,
        String contentType,
        String topic,
        Long imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden
    ) {
        return new ShopContentBoardJpaEntity(shopId, contentType, topic, imageFileId, youtubeUrl, description, hidden);
    }

    void applyChanges(String topic, Long imageFileId, String youtubeUrl, String description, boolean hidden) {
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
        this.hidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getTopic() {
        return this.topic;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public String getYoutubeUrl() {
        return this.youtubeUrl;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
