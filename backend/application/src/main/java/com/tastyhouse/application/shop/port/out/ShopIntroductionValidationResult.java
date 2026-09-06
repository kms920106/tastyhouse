package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopIntroductionValidationResult(
    boolean valid,
    List<String> violations
) {
}
