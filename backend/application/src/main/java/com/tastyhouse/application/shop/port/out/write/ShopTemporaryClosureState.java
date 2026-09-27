package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShopTemporaryClosureState(
    Long id,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate,
    LocalDateTime createdAt
) {
}
