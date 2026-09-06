package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopContentBoard {
    private static final int MAX_DESCRIPTION_LENGTH = 50;
    private static final Pattern YOUTUBE_URL_PATTERN =
        Pattern.compile("^https?://(www\\.)?(youtube\\.com/watch|youtu\\.be/).+$");

    private final Long id;
    private final ShopId shopId;
    private final ShopContentType contentType;
    private ShopContentTopic topic;
    private UploadedFileId imageFileId;
    private String youtubeUrl;
    private String description;
    private boolean hidden;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopContentBoard(
        Long id,
        ShopId shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        UploadedFileId imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.contentType = contentType;
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
        this.hidden = hidden;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopContentBoard of(
        ShopId shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        UploadedFileId imageFileId,
        String youtubeUrl,
        String description
    ) {
        validate(contentType, imageFileId, youtubeUrl, description);
        return new ShopContentBoard(null, shopId, contentType, topic, imageFileId, youtubeUrl, description, false, null, null);
    }

    public static ShopContentBoard reconstitute(
        Long id,
        ShopId shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        UploadedFileId imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopContentBoard(id, shopId, contentType, topic, imageFileId, youtubeUrl, description, hidden, createdAt, updatedAt);
    }

    public void update(ShopContentTopic topic, UploadedFileId imageFileId, String youtubeUrl, String description) {
        validate(this.contentType, imageFileId, youtubeUrl, description);
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    private static void validate(ShopContentType contentType, UploadedFileId imageFileId, String youtubeUrl, String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_DESCRIPTION_TOO_LONG);
        }

        if (contentType == ShopContentType.VIDEO) {
            if (!isValidYoutubeUrl(youtubeUrl)) {
                throw new BusinessException(ErrorCode.SHOP_CONTENT_YOUTUBE_URL_INVALID);
            }
        } else if (imageFileId == null) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID);
        }
    }

    public static boolean isValidYoutubeUrl(String url) {
        return url != null && YOUTUBE_URL_PATTERN.matcher(url).matches();
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopContentType getContentType() {
        return this.contentType;
    }

    public ShopContentTopic getTopic() {
        return this.topic;
    }

    public UploadedFileId getImageFileId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
