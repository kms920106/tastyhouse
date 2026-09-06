package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopVisitGuideValidationResult(
    boolean valid,
    List<String> violations
) {
}
