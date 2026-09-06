package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopOriginInfo {
    private static final int CONTENT_MAX_LENGTH = 2000;
    private static final int URL_MAX_LENGTH = 500;
    private static final String HTTP_SCHEME = "http://";
    private static final String HTTPS_SCHEME = "https://";

    private final Long id;
    private final ShopId shopId;
    private OriginSourceType sourceType;
    private String content;
    private String url;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopOriginInfo(
        Long id,
        ShopId shopId,
        OriginSourceType sourceType,
        String content,
        String url,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.sourceType = sourceType;
        this.content = content;
        this.url = url;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopOriginInfo of(ShopId shopId, OriginSourceType sourceType, String content, String url) {
        validate(sourceType, content, url);

        return new ShopOriginInfo(null, shopId, sourceType, contentFor(sourceType, content), urlFor(sourceType, url),
            null, null);
    }

    public static ShopOriginInfo reconstitute(
        Long id,
        ShopId shopId,
        OriginSourceType sourceType,
        String content,
        String url,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopOriginInfo(id, shopId, sourceType, content, url, createdAt, updatedAt);
    }

    public void update(OriginSourceType sourceType, String content, String url) {
        validate(sourceType, content, url);

        this.sourceType = sourceType;
        this.content = contentFor(sourceType, content);
        this.url = urlFor(sourceType, url);
    }

    private static void validate(OriginSourceType sourceType, String content, String url) {
        if (sourceType == null) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN);
        }
        if (sourceType == OriginSourceType.DIRECT) {
            validateContent(content);
            return;
        }
        validateUrl(url);
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_CONTENT_REQUIRED);
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_CONTENT_TOO_LONG);
        }
    }

    private static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_URL_REQUIRED);
        }
        if (url.length() > URL_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_URL_TOO_LONG);
        }
        if (!url.startsWith(HTTP_SCHEME) && !url.startsWith(HTTPS_SCHEME)) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_URL_INVALID);
        }
    }

    private static String contentFor(OriginSourceType sourceType, String content) {
        return sourceType == OriginSourceType.DIRECT ? content : null;
    }

    private static String urlFor(OriginSourceType sourceType, String url) {
        return sourceType == OriginSourceType.FRANCHISE_URL ? url : null;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
