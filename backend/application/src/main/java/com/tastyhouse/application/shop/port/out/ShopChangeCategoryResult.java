package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.CodeLabelResult;

public record ShopChangeCategoryResult(
    String category,
    String categoryDescription,
    List<CodeLabelResult> changeTypes
) {
}
