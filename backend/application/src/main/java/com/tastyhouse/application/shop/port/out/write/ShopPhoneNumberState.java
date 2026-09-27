package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopPhoneNumberState(
    Long id,
    Long shopId,
    String phoneNumber,
    boolean primary,
    boolean virtual,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
