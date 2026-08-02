package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDate;


public record ShopTemporaryClosureResult(
    Long id,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {

}
