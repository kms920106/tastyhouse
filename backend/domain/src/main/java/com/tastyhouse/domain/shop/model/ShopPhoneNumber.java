package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopPhoneNumber {
    private static final List<String> VIRTUAL_NUMBER_PREFIXES = List.of(
        "02", "15", "16", "18",
        "051", "053", "032", "062", "042", "052", "033",
        "031", "043", "041", "044", "063", "061", "054", "055", "064", "070", "010"
    );
    private static final int VIRTUAL_NUMBER_MIN_LENGTH = 8;
    private static final int VIRTUAL_NUMBER_MAX_LENGTH = 13;

    private final Long id;
    private final ShopId shopId;
    private final String phoneNumber;
    private boolean primary;
    private final boolean virtual;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopPhoneNumber(
        Long id,
        ShopId shopId,
        String phoneNumber,
        boolean primary,
        boolean virtual,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.phoneNumber = phoneNumber;
        this.primary = primary;
        this.virtual = virtual;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopPhoneNumber of(ShopId shopId, String phoneNumber, boolean primary, boolean virtual) {
        if (virtual && !isValidVirtualNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.SHOP_VIRTUAL_NUMBER_INVALID);
        }
        return new ShopPhoneNumber(null, shopId, phoneNumber, primary, virtual, null, null);
    }

    public static ShopPhoneNumber reconstitute(
        Long id,
        ShopId shopId,
        String phoneNumber,
        boolean primary,
        boolean virtual,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopPhoneNumber(id, shopId, phoneNumber, primary, virtual, createdAt, updatedAt);
    }

    public void markPrimary() {
        this.primary = true;
    }

    public void unmarkPrimary() {
        this.primary = false;
    }

    public static boolean isValidVirtualNumber(String raw) {
        if (raw == null) {
            return false;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < VIRTUAL_NUMBER_MIN_LENGTH || digits.length() > VIRTUAL_NUMBER_MAX_LENGTH) {
            return false;
        }
        return VIRTUAL_NUMBER_PREFIXES.stream().anyMatch(digits::startsWith);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
