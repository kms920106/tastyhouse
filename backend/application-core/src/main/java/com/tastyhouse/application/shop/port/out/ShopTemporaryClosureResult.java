package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

public record ShopTemporaryClosureResult(
    Long id,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {
}
